package org.archive.accesscontrol.webui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.accesscontrol.model.HibernateRuleDao;
import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleSet;
import org.archive.surt.NewSurtTokenizer;
import org.archive.util.ArchiveUtils;
import org.archive.util.SURT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

public class AdminController extends AbstractController {
    private HibernateRuleDao ruleDao;
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final long NEW_RULE = -1L;
    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    protected enum ErrorStatus
    {
    	SUCCESS,
    	DUP_RULE,
    }

    @Autowired
    public AdminController(HibernateRuleDao ruleDao) {
        this.ruleDao = ruleDao;        
    }
    
    protected ModelAndView ruleList(String surt, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Long editingRuleId = null;
        if (request.getParameter("edit") != null) {
            if (request.getParameter("edit").equals("new")) {
                editingRuleId = NEW_RULE;
            } else {
                try {
                    editingRuleId = Long.decode(request.getParameter("edit"));
                } catch (NumberFormatException e) {
                }
            }
            
        }
        return ruleList(surt, editingRuleId, request, response);
    }

    /**
     * Return true if the given string appears to be a SURT.
     * @param s
     * @return
     */
    protected boolean isSurt(String s) {
        return s.charAt(0) == '(' || s.indexOf("://") == s.indexOf("://(");
    }
    
    /**
     * Perform a several cleanups on the given surt:
     *   * Convert a URL to a SURT
     *   * Add a trailing slash to SURTs of the form: http://(...)
     * @param surt
     * @return
     */
    protected String cleanSurt(String surt) {
        if (!isSurt(surt)) {
            surt = ArchiveUtils.addImpliedHttpIfNecessary(surt);
            surt = SURT.fromURI(surt);
        }
        
        if (surt.endsWith(",)") && surt.indexOf(")") == surt.length()-1) {
            surt = surt + "/";
        }
        
        return surt;
    }
    
    protected ModelAndView ruleList(String surt, Long editingRuleId, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        surt = cleanSurt(surt);        
        int surtSegments = new NewSurtTokenizer(surt).toList().size();
        Map<String, Object> model = new HashMap<String, Object>();
        RuleSet rules = ruleDao.getRuleTree(surt);
        ArrayList<DisplayRule> ruleList = new ArrayList<DisplayRule>();
        ArrayList<String> childSurts = new ArrayList<String>();
        
        for (Rule rule: rules) {
            int comparison = rule.getSurt().compareTo(surt);
            if (comparison <= 0) {
                DisplayRule displayRule = new DisplayRule(rule, comparison != 0);
                displayRule.setEditing(rule.getId().equals(editingRuleId));
                ruleList.add(displayRule);
            } else {
                try {
                String segment = new NewSurtTokenizer(rule.getSurt())
                            .toList().get(surtSegments);
                    if (!childSurts.contains(segment)) {
                        childSurts.add(segment);
                    }
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }
        String order = request.getParameter("order");
        boolean reverse = order != null && order.equals("1");
        
        if (!reverse) {
            Collections.sort(ruleList, Collections.reverseOrder());
        } else {
        	Collections.sort(ruleList);
        }
        
        if (editingRuleId != null && editingRuleId == NEW_RULE) {
            Rule rule = new Rule();
            rule.setId(NEW_RULE);
            rule.setSurt(surt);
            
            DisplayRule newRule = new DisplayRule(rule, false);
            newRule.setEditing(true);
            ruleList.add(newRule);
        }
        
        ArrayList<String> childSurtsList = new ArrayList<String>(childSurts); 
        Collections.sort(childSurtsList);
        
        doURLCheck(rules, ruleList, request, model); 
                
        model.put("rules", ruleList);
        model.put("surt", surt);
        model.put("childSurts", childSurtsList);
        model.put("encodedSurt", URLEncoder.encode(surt, "utf-8"));
        model.put("breadcrumbs", SurtNode.nodesFromSurt(surt));
        model.put("editingRuleId", request.getParameter("edit"));
        model.put("errorStatus", request.getParameter("errorStatus"));
        return new ModelAndView("list_rules", model);
    }
    
    protected void doURLCheck(RuleSet rules, List<DisplayRule> ruleList, HttpServletRequest request, Map<String, Object> model) {
		
		String url = request.getParameter("checkURL");

		if (url == null || url.isEmpty()) {
			return;
		}
		
		String surt;
		
		if (this.isSurt(url)) {
			surt = url;
		} else {
	        url = ArchiveUtils.addImpliedHttpIfNecessary(url);
	        surt = SURT.fromURI(url);
		}
		surt = this.cleanSurt(surt);
		
		String dateStamp = request.getParameter("checkDate");
		String group = request.getParameter("checkGroup");
		model.put("checkGroup", group);
		model.put("checkURL", url);		
		
		Date captureDate = null;
		
		if ((dateStamp != null) && !dateStamp.isEmpty()) {
			String paddedDateStr = dateStamp;
			int pad = 14 - dateStamp.length();
			for (int i = 0; i < pad; i++) {
				paddedDateStr += '0';
			}
			
			try {
				captureDate = ArchiveUtils.parse14DigitDate(paddedDateStr);
				model.put("checkDate", dateStamp);				
			} catch (ParseException e) {
				captureDate = null;
			}
		}
		
		Date retrievalDate = new Date();
		
		if (captureDate == null) {
			captureDate = retrievalDate;
		}
		
		Rule theRule = rules.getMatchingRule(surt, captureDate, retrievalDate, group);
		
		if (theRule == null) {
			return;
		}
		
		// Now, find displayRule that contains matched rule, if any, and set it to highlight
		for (DisplayRule displayRule : ruleList)
		{
			if (displayRule.getRule().getId().equals(theRule.getId())) {
				displayRule.setHighlight(true);
				break;
			}
		}
	}
    
    protected ModelAndView redirectToSurt(HttpServletRequest request, HttpServletResponse response, String surt, ErrorStatus errStatus) throws UnsupportedEncodingException {
        String newUrl = request.getContextPath() + "/admin?surt=" + URLEncoder.encode(surt, "UTF-8");
        if (errStatus != ErrorStatus.SUCCESS) {
        	newUrl += "&errorStatus=" + errStatus.toString();
        }
    	response.setHeader("Location", newUrl);
        response.setStatus(302);
        return null;
    }
    
    protected ModelAndView redirectToSurt(HttpServletRequest request, HttpServletResponse response, String surt) throws UnsupportedEncodingException {
    	return redirectToSurt(request, response, surt, ErrorStatus.SUCCESS);
    }
    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (request.getParameter("saveRule") != null) {
            return saveRule(request, response);
        }
              
        
        String surt = (String) request.getAttribute("id");
        if (surt == null) {
            surt = request.getParameter("surt");
        }
        
        if (request.getParameter("cancel") != null) {
            return redirectToSurt(request, response, surt);
        }
        
        if (request.getParameter("delete") != null) {
            return deleteRule(request, response);
        }
        
        if (surt != null) {
            return ruleList(surt, request, response);
        }
        
        return new ModelAndView("index");
    }

    private ModelAndView deleteRule(HttpServletRequest request,
            HttpServletResponse response) throws UnsupportedEncodingException {
        Long ruleId = Long.decode(request.getParameter("edit"));
        ruleDao.deleteRule(ruleId);
        return redirectToSurt(request, response, request.getParameter("surt"));
    }

    private ModelAndView saveRule(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        String surt = request.getParameter("surt");
        
        Rule rule;
        Long ruleId = Long.decode(request.getParameter("edit"));
        if (ruleId == NEW_RULE) {
            rule = new Rule();
        } else {
            rule = ruleDao.getRule(ruleId);
        }
        rule.setSurt(surt);
        rule.setPolicy(request.getParameter("policy"));
        rule.setWho(request.getParameter("who"));
        rule.setCaptureStart(parseDate(request.getParameter("captureStart")));
        rule.setCaptureEnd(parseDate(request.getParameter("captureEnd")));
        rule.setRetrievalStart(parseDate(request.getParameter("retrievalStart")));
        rule.setRetrievalEnd(parseDate(request.getParameter("retrievalEnd")));
        rule.setSecondsSinceCapture(parseInteger(request.getParameter("secondsSinceCapture")));
        rule.setPrivateComment(request.getParameter("privateComment"));
        rule.setPublicComment(request.getParameter("publicComment"));
        rule.setExactMatch(request.getParameter("exactMatch") != null);
        
        boolean saved = true;
        
        // If adding a new rule, make sure it doesn't match any existing rules
        // or we'll have duplicates (and only one of the dups will show up in the list)
        if (ruleId == NEW_RULE) {
        	saved = ruleDao.saveRuleIfNotDup(rule);
        } else {
        	ruleDao.saveRule(rule);
        }
        
        return redirectToSurt(request, response, surt, saved ? ErrorStatus.SUCCESS : ErrorStatus.DUP_RULE);
    }
    
    private Date parseDate(String s) {
        try {
            return dateFormatter.parse(s);
        } catch (ParseException e) {
            return null;
        }
    }
    
    private Integer parseInteger(String s) {
        try {
            return Integer.decode(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
