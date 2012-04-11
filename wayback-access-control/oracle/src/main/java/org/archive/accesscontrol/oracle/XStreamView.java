package org.archive.accesscontrol.oracle;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.accesscontrol.model.Rule;
import org.archive.accesscontrol.model.RuleChange;
import org.archive.accesscontrol.model.RuleSet;
import org.springframework.web.servlet.View;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * The XStreamView uses XStream to serialize a given object as XML or JSON.
 * 
 */
public class XStreamView implements View {
    private XStream xstream;
    private String contentType = "application/xml";

    public XStreamView(String format) {
        if (format.equals("json")) {
            contentType = "application/json";
            xstream = new XStream(new JettisonMappedXmlDriver());
        } else if (format.equals("xml")) {
            contentType = "application/xml";
            xstream = new XStream();
        }
        configureXStream();
    }

    public XStreamView(XStream xstream, String contentType) {
        this.contentType = contentType;
        this.xstream = xstream;
        configureXStream();
    }

    private void configureXStream() {
        xstream.alias("rule", Rule.class);
        xstream.alias("ruleSet", RuleSet.class);
        xstream.alias("error", SimpleError.class);
        xstream.alias("ruleChange", RuleChange.class);
    }

    public String getContentType() {
        return contentType;
    }

    public void render(Map model, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Object obj = model.get("object");

        if (obj instanceof SimpleError) {
            response.setStatus(((SimpleError) obj).getStatus());
        }

        if (model.containsKey("status")) {
            response.setStatus((Integer) model.get("status"));
        }
        response.setContentType(getContentType());
        xstream.toXML(obj, response.getOutputStream());
    }

    /**
     * @return the xstream
     */
    public XStream getXstream() {
        return xstream;
    }
}
