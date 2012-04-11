package org.archive.accesscontrol.oracle;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.View;

/**
 * View returning the HTTP 201 Created response, to indicate a resouce was
 * created successfully.
 */
public class CreatedView implements View {

    private String path;

    public CreatedView(String path) {
        super();
        this.path = path;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    public String getContentType() {
        return null;
    }

    public void render(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        response.addHeader("Location", getContextUrl(request) + path);
        response.setStatus(201);
    }

    /**
     * Return the full URL of the context.
     * 
     * eg. http://localhost:8080/exclusions-oracle-0.0.1-SNAPSHOT
     * 
     * @param request
     * @return
     */
    public String getContextUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String context = request.getContextPath();
        String contextUrl = url.substring(0, url.indexOf(context)
                + context.length());
        return contextUrl;
    }
}
