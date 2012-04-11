package org.archive.accesscontrol.oracle;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.archive.accesscontrol.model.HibernateRuleDao;
import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleChange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;


public class RulesController extends AbstractController {
    private HibernateRuleDao ruleDao;
    private AutoFormatView view;

    @Autowired
    public RulesController(HibernateRuleDao ruleDao, AutoFormatView view) {
        this.ruleDao = ruleDao;
        this.view = view;

        String[] methods = { "GET", "PUT", "DELETE", "POST" };
        this.setSupportedMethods(methods);
    }

    /**
     * GET /rules/:id
     * 
     * Retrieves the rule with the given id.
     * 
     * @param id
     * @return
     */
    public ModelAndView getRule(long id) {
        Rule rule = ruleDao.getRule(id);

        if (rule == null) {
            return new ModelAndView(view, "object", new SimpleError("Rule "
                    + id + " does not exist.", 404));
        }

        return new ModelAndView(view, "object", rule);
    }

    /**
     * PUT /rules/:id
     * 
     * Updates the rule with the given id, noting the change in the change log.
     * 
     * Optional extra headers: Comment - A comment that will appear in the
     * change log. User - End-user who requested the change.
     * 
     * @param id
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public ModelAndView putRule(long id, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        Rule oldRule = ruleDao.getRule(id);
        if (oldRule == null) {
            return new ModelAndView(
                    view,
                    "object",
                    new SimpleError(
                            "Rule "
                                    + id
                                    + " does not exist.  To create a new rule POST it to /rules",
                            404));
        }

        Rule newRule = (Rule) view.deserializeRequest(request);
        newRule.setId(id);

        String comment = "Changed by REST client: "
                + request.getHeader("User-agent");
        if (request.getHeader("Comment") != null) {
            comment = request.getHeader("Comment");
        }

        String user = "" + request.getRemoteUser() + "@"
                + request.getRemoteAddr();
        if (request.getHeader("User") != null) {
            user = request.getHeader("User") + " via " + user;
        }

        RuleChange change = new RuleChange(oldRule, RuleChange.UPDATED,
                new Date(), user, comment);
        change.setRule(newRule);

        ruleDao.saveRule(newRule, change);
        return new ModelAndView(view, "object", change);
    }

    /**
     * DELETE /rules/:id
     * 
     * Deletes the given rule.
     * 
     * Optional extra headers: Comment - A comment that will appear in the
     * change log. User - End-user who requested the change.
     * 
     * @param id
     * @param response
     * @return
     * @throws IOException
     */
    public ModelAndView deleteRule(long id, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        String comment = "Deleted by REST client: "
                + request.getHeader("User-agent");
        if (request.getHeader("Comment") != null) {
            comment = request.getHeader("Comment");
        }

        String user = "" + request.getRemoteUser() + "@"
                + request.getRemoteAddr();
        if (request.getHeader("User") != null) {
            user = request.getHeader("User") + " via " + user;
        }

        Rule rule = ruleDao.getRule(id);
        if (rule != null) {
            RuleChange change = new RuleChange(rule, RuleChange.DELETED,
                    new Date(), user, comment);
            ruleDao.deleteRule(id);
            ruleDao.saveChange(change);
        }
        response.setStatus(204);
        return null;
    }

    /**
     * POST /rules
     * 
     * Creates a new rule. The URL of the new rule will be returned in the
     * Location header.
     * 
     * You can also POST a list of rules to do batch creation.
     * 
     * Optional extra headers: Comment - A comment that will appear in the
     * change log. User - End-user who requested the change.
     * 
     * @param request
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public ModelAndView postNewRule(HttpServletRequest request)
            throws IOException {
        Object data = view.deserializeRequest(request);
        
        Collection<Rule> rules;
        if (data instanceof Collection) {
            rules = (Collection<Rule>) data;
        } else if (data instanceof Rule) {
            rules = new LinkedList<Rule>();
            rules.add((Rule)data);
        } else {
            return new ModelAndView(view, "object", new SimpleError(
                    "Expected a rule or collection of rules.", 400));
        }

        long id = 0;
        for (Rule rule : rules) {
            rule.setId(null);

            String comment = "Created by REST client: "
                    + request.getHeader("User-agent");
            if (request.getHeader("Comment") != null) {
                comment = request.getHeader("Comment");
            }

            String user = "" + request.getRemoteUser() + "@"
                    + request.getRemoteAddr();
            if (request.getHeader("User") != null) {
                user = request.getHeader("User") + " via " + user;
            }

            RuleChange change = new RuleChange(rule, RuleChange.CREATED,
                    new Date(), user, comment);

            ruleDao.saveRule(rule, change);
            id = rule.getId();
        }
        return new ModelAndView(new CreatedView("/rules/" + id));
    }

    /**
     * GET /rules
     * 
     * Retrieves a set of rules.
     * 
     * Optional parameters: prefix - A SURT prefix to filter by. surt - An exact
     * surt to return.
     * 
     * @param request
     * @return
     */
    public ModelAndView getRules(HttpServletRequest request) {
        String prefix = request.getParameter("prefix");
        if (prefix != null) {
            return new ModelAndView(view, "object", ruleDao
                    .getRulesWithSurtPrefix(prefix));
        }

        String surt = request.getParameter("surt");
        if (surt != null) {
            return new ModelAndView(view, "object", ruleDao
                    .getRulesWithExactSurt(surt));
        }

        return new ModelAndView(view, "object", ruleDao.getAllRules());
    }
    
    /**
     * GET /rules/(some,surt,)
     * 
     * @param surt
     * @return
     * @throws URIException
     */
    public ModelAndView getRuleNode(String surt) throws URIException {
        return new ModelAndView(view, "object", ruleDao.getRulesWithExactSurt(surt));        
    }
    
    /**
     * GET /rules/tree/(some,surt,)
     * 
     * @param surt
     * @return
     * @throws URIException
     */
    public ModelAndView getRuleTree(String surt) throws URIException {
    	surt = fixupSchemeSlashes(surt);
    	System.out.println("*** getRuleTree: " + surt);
        return new ModelAndView(view, "object", ruleDao.getRuleTree(surt));        
    }

    /**
     * Ensure scheme has a double slash. eg replaces "http:/blah" with "http://blah"
     */ 
	private String fixupSchemeSlashes(String surt) {
		if (surt.indexOf(":/(") == surt.indexOf(":")) {
    		int i = surt.indexOf(":");
    		surt = surt.substring(0, i + 1) + "/" + surt.substring(i+1);
    	}
		return surt;
	}

    
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String idParam = request.getParameter("id");
        if (idParam == null) {
            idParam = (String)request.getAttribute("id");
        }
        
        if (idParam == null) {
            String tree = (String) request.getAttribute("tree"); 
            if (tree != null) {                              // GET /rules/tree/:tree
                return getRuleTree(tree);
            } else if (request.getMethod().equals("POST")) { // POST /rules
                return postNewRule(request);
            } else if (request.getMethod().equals("GET")) {  // GET /rules 
                return getRules(request);
            } else if (request.getMethod().equals("DELETE")) { // DELETE /rules
                // FIXME: this is useful for testing but is dangerous
                ruleDao.deleteAllRules();
                return null;
            }
        }
        
        Long id = null;
        try {
        	id = new Long(idParam);
        } catch (NumberFormatException e) {}
        
        if (id == null) {                // GET /rules/(some,surt,)
            return getRuleNode(idParam);
        } else {
            if (request.getMethod().equals("GET")) {         // GET /rules/:id
                return getRule(id);
            } else if (request.getMethod().equals("PUT")) {  // PUT /rules/:id
                return putRule(id, request, response);
            } else if (request.getMethod().equals("DELETE")) { // DELETE /rules/:id
                return deleteRule(id, request, response);
            }
        }
        return new ModelAndView(view, "object", new SimpleError(
                "Method not acceptable: " + request.getMethod(), 405));
    }
}
