/* ExclusionResponse
 *
 * $Id$
 *
 * Created on 4:30:17 PM Feb 13, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol;


import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Abstraction for the result of an exclusion query, and generating the
 * differentiating parts of an HTTP response. The original implementation
 * returned XML. Without the XML, this class is really not needed, but as
 * I suspect we'll return to an XML HTTML response in the future, including 
 * this for now.
 *  
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ExclusionResponse {
	
	private boolean USE_XML = false;
	
	private String responseType = null;

	private String hostname = null;

	private boolean authorized = false;
	
	private String message = null;

	/**
	 * Constructor
	 * 
	 * @param hostname
	 * @param responseType
	 * @param authorized
	 * @param message
	 */
	public ExclusionResponse(final String hostname, final String responseType,
			final boolean authorized,final String message) {
		this.hostname = hostname;
		this.responseType = responseType;
		this.authorized = authorized;
		this.message = message;
	}
	
	/**
	 * Constuctor
	 * 
	 * @param hostname
	 * @param responseType
	 * @param authorized
	 */
	public ExclusionResponse(final String hostname, final String responseType,
			final boolean authorized) {
		
		this.hostname = hostname;
		this.responseType = responseType;
		this.authorized = authorized;
		this.message = "";
	}

	/**
	 * Send the HTTP message body to requesting client, via the OutputStream
	 * 
	 * @param os
	 */
	public void writeResponse(OutputStream os) {
		if(USE_XML) {
			writeResponseXML(os);
		} else {
			writeResponseText(os);
		}
	}
	
	/**
	 * @return String HTTP Content-Type field: "text/xml" or "text/plain"
	 */
	public String getContentType() {
		if(USE_XML) {
			return getContentTypeXML();
		} else {
			return getContentTypeText();
		}
	}

	private String getContentTypeText() {
		return "text/plain";
	}

	private void writeResponseText(OutputStream os) {

		String content = authorized ? "OK" : "BLOCKED - " + message;
		try {
			os.write(content.getBytes());
		} catch (IOException e) {
			// TODO what now?
			e.printStackTrace();
		}
	}
	
	private String getContentTypeXML() {
		return "text/xml";
	}

	private void writeResponseXML(OutputStream os) {

		Document doc;
		try {
			doc = getResponseXMLDocument();
			DOMSource source = new DOMSource(doc);
			TransformerFactory transFactory = TransformerFactory.newInstance();
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		} catch (ParserConfigurationException e) {
			// TODO is anything output here?
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO is anything output here?
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO is anything output here?
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Document getResponseXMLDocument() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document doc = factory.newDocumentBuilder().newDocument();
		Element response = addNode(doc, doc, "response");
		addAttribute(doc,response,"type",responseType);
		addAttribute(doc,response,"hostname",hostname);
		addAttribute(doc,response,"authorized",authorized?"true":"false");
		addAttribute(doc,response,"message",message);
		return doc;
	}

	private static Element addNode(Document doc, Node parent, String name) {
		Element child = doc.createElement(name);
		parent.appendChild(child);
		return child;
	}
	
	// All below here stolen from NutchwaxOpenSearchServlet...

//	private static void addNode(Document doc, Node parent, String name,
//			String text) {
//		Element child = doc.createElement(name);
//		child.appendChild(doc.createTextNode(getLegalXml(text)));
//		parent.appendChild(child);
//	}

	private static void addAttribute(Document doc, Element node, String name,
			String value) {
		Attr attribute = doc.createAttribute(name);
		attribute.setValue(getLegalXml(value));
		node.getAttributes().setNamedItem(attribute);
	}

	/*
	 * Ensure string is legal xml.
	 * First look to see if string has illegal characters.  If it doesn't,
	 * just return it.  Otherwise, create new string with illegal characters
	 * @param text String to verify.
	 * @return Passed <code>text</code> or a new string with illegal
	 * characters removed if any found in <code>text</code>.
	 * @see http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char
	 */
	private static String getLegalXml(final String text) {
		if (text == null) {
			return null;
		}
		boolean allLegal = true;
		for (int i = 0; i < text.length(); i++) {
			if (!isLegalXml(text.charAt(i))) {
				allLegal = false;
				break;
			}
		}
		return allLegal ? text : createLegalXml(text);
	}

	private static String createLegalXml(final String text) {
		if (text == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (isLegalXml(c)) {
				buffer.append(c);
			}
		}
		return buffer.toString();
	}

	private static boolean isLegalXml(final char c) {
		return c == 0x9 || c == 0xa || c == 0xd || (c >= 0x20 && c <= 0xd7ff)
				|| (c >= 0xe000 && c <= 0xfffd)
				|| (c >= 0x10000 && c <= 0x10ffff);
	}
	/**
	 * @return Returns the authorized.
	 */
	public boolean isAuthorized() {
		return authorized;
	}
	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}
}
