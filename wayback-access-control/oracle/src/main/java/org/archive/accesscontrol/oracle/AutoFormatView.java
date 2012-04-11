package org.archive.accesscontrol.oracle;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

/**
 * AutoFormatView renders an object to XML or JSON, depending on whether a
 * "format" parameter or attribute was supplied.
 * 
 * eg. to request json: /rules/1.json
 *                  or: /rule?id=1&format=json
 * 
 */
public class AutoFormatView implements View {
    private Map<String, View> views;
    private String defaultFormat;
    

    /**
     * @return the views
     */
    public Map<String, View> getViews() {
        return views;
    }
    
    public View getView(String format) {
        View view = views.get(format);
        if (view == null) {
            view = views.get(defaultFormat);
        }
        return view;
    }

    /**
     * @param views the views to set
     */
    public void setViews(Map<String, View> views) {
        this.views = views;
    }

    /**
     * @return the defaultFormat
     */
    public String getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * @param defaultFormat the defaultFormat to set
     */
    public void setDefaultFormat(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public String getContentType() {
        return "application/xml";
    }

    public void render(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        System.out.println("views = " + views + " defaultFormat =" + defaultFormat);
        String format = request.getParameter("format");

        if (format == null || format.equals("")) {
            format = (String) request.getAttribute("format");
        }
        
        getView(format).render(model, request, response);        
    }

    /**
     * Return the first view with the given content type.
     * @param contentType
     * @return
     */
    public View viewByContentType(String contentType) {
        for (View view: views.values()) {
            if (view.getContentType().equals(contentType)) {
                return view;
            }
        }
        return null;
    }
    
    public Object deserializeRequest(HttpServletRequest request) throws IOException {
        String ctype = request.getContentType();
        if (ctype == null) {
            ctype = "application/xml";
        }
        XStreamView view = (XStreamView)viewByContentType(ctype);
        return view.getXstream().fromXML(request.getInputStream());
    }
}
