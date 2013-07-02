package org.archive.wayback.memento;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.AccessPoint;
/*
import org.dspace.foresite.Agent;
import org.dspace.foresite.AggregatedResource;
import org.dspace.foresite.Aggregation;
import org.dspace.foresite.OREException;
import org.dspace.foresite.OREFactory;
import org.dspace.foresite.ORESerialiser;
import org.dspace.foresite.ORESerialiserException;
import org.dspace.foresite.ORESerialiserFactory;
import org.dspace.foresite.Predicate;
import org.dspace.foresite.ResourceMap;
import org.dspace.foresite.ResourceMapDocument;
import org.dspace.foresite.Triple;
import org.dspace.foresite.jena.TripleJena;
*/

public class MementoRDFSerializer {
	
	/*
	private final static String FORESITE_TOOLKIT = "Foresite Toolkit (Java)";
	private final static String TIME_BUNDLE_TITLE = "Memento Time Bundle for ";
	private final static String URN_UUID = "urn:uuid:";
	
	// all the constant URIs needed:
	private static URI RDF_22_SYNTAX_URI = null;
	private static URI TIMEMAP_TERM_URI = null;
	private static URI TIMEGATE_TERM_URI = null;
	private static URI TIMEBUNDLE_TERM_URI = null;
	private static URI TIMEBUNDLE_START_TERM_URI = null;
	private static URI TIMEBUNDLE_END_TERM_URI = null;
	private static URI ORIGINAL_RESOURCE_TERM_URI = null;
	private static URI PURL_11_ELEMENTS_URI = null;
	private static URI MEMENTO_TERM_URI = null;
	private static URI MEMENTO_FOR_TERM_URI = null;
	private static URI MEMENTO_OBSERVED_OVER_TERM_URI = null;
	private static URI MEMENTO_PERIOD_TERM_URI = null;
	
	
	static {
		try {
			RDF_22_SYNTAX_URI = 
				new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			TIMEMAP_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/TimeMap");
			TIMEGATE_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/TimeGate");
			TIMEBUNDLE_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/TimeBundle");
			TIMEBUNDLE_START_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/start");
			TIMEBUNDLE_END_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/end");
			ORIGINAL_RESOURCE_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/OriginalResource");
			PURL_11_ELEMENTS_URI = 
				new URI("http://purl.org/dc/elements/1.1/format");
			MEMENTO_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/Memento");
			MEMENTO_FOR_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/mementoFor");
			MEMENTO_OBSERVED_OVER_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/observedOver");
			MEMENTO_PERIOD_TERM_URI = 
				new URI("http://www.mementoweb.org/terms/tb/Period");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void writeRDF(HttpServletRequest request, 
			HttpServletResponse response, UIResults results) 
	throws URISyntaxException, OREException, ORESerialiserException, 
	IOException {


//	    SimpleDateFormat httpformatterl = new SimpleDateFormat(
//	            "E, dd MMM yyyy HH:mm:ss z");
//	    TimeZone tzo = TimeZone.getTimeZone("GMT");
//	    httpformatterl.setTimeZone(tzo);
//		String format = MementoConstants.FORMAT_RDF;
//	    SimpleDateFormat timestampFormatter = 
//	    	MementoUtils.DATE_FORMAT_14_FORMATTER;

	    WaybackRequest wbRequest = results.getWbRequest();
	    AccessPoint accessPoint = wbRequest.getAccessPoint();
	    CaptureSearchResults cResults = results.getCaptureResults();
	    
//	    CaptureSearchResult res = cResults.getClosest();
	    ResultURIConverter uriC = results.getURIConverter();
//	    String replayPrefix = wbRequest.getAccessPoint().getReplayPrefix();
//	    String queryPrefix = wbRequest.getAccessPoint().getQueryPrefix();
//	    Properties apProps = wbRequest.getAccessPoint().getConfigs();
	    
//	    String timegatePrefix = apProps.getProperty("timegatePrefix") + "timegate/";
	    String requestURL = wbRequest.getRequestUrl();

	    String timeMapUrl = MementoUtils.getTimemapUrl(accessPoint,
	    		MementoConstants.FORMAT_RDF,requestURL);
	    String timeBundleUrl = MementoUtils.getTimebundleUrl(accessPoint, requestURL);
	    String timeGateUrl = MementoUtils.getTimegateUrl(accessPoint, requestURL);
	    
	    // TODO: URIException handling?
	    URI requestURI = new URI(requestURL);
	    URI timeMapURI = new URI(timeMapUrl);
	    URI timeBundleURI = new URI(timeBundleUrl);
	    URI timeGateURI = new URI(timeGateUrl);

	    
//	    String format = wbRequest.get("format");
//	    if(format == null) {
//	        format = "rdf";
//	    }
	    Aggregation aggregation = OREFactory.createAggregation(timeBundleURI);
	    ResourceMap resourceMap = aggregation.createResourceMap(timeMapURI);

	    Date now = new Date();

	    resourceMap.setCreated(now);
	    Predicate rdfPredicate = new Predicate();
	    rdfPredicate.setURI(RDF_22_SYNTAX_URI);

	    resourceMap.setModified(now);
	    resourceMap.createTriple(rdfPredicate,TIMEMAP_TERM_URI);
	    Agent creator = OREFactory.createAgent();
	    creator.addName(FORESITE_TOOLKIT);

	    resourceMap.addCreator(creator);
	    aggregation.addTitle(TIME_BUNDLE_TITLE + requestURL);

	    Iterator<CaptureSearchResult> itr = cResults.iterator();

	    // For what was this going to be used?
//	    Date f = cResults.getFirstResultDate();
//	    Date l = cResults.getLastResultDate();
//	    String ArchiveInterval = timestampFormatter.format(f) + " - "
//	            + timestampFormatter.format(l);

	    aggregation.addType(TIMEBUNDLE_TERM_URI);
	    //include original into aggregation

	    AggregatedResource aggResource = 
	    	aggregation.createAggregatedResource(requestURI);

	    aggResource.createTriple(rdfPredicate, ORIGINAL_RESOURCE_TERM_URI);

	    //include timegate into aggregation
	    AggregatedResource aggTimegate = aggregation.createAggregatedResource(timeGateURI);

	    Predicate purlPredicate = new Predicate();
	    purlPredicate.setURI(PURL_11_ELEMENTS_URI);

	    aggTimegate.createTriple(purlPredicate, requestURI);
	    aggTimegate.createTriple(rdfPredicate, TIMEGATE_TERM_URI);

	    String previos_digest = null;
	    ArrayList<String> previos_blancs = new ArrayList<String>();

	    Predicate startPredicate = new Predicate();
	    startPredicate.setURI(TIMEBUNDLE_START_TERM_URI);
	    Predicate endPredicate = new Predicate();
	    endPredicate.setURI(TIMEBUNDLE_END_TERM_URI);

	    Calendar cal = Calendar.getInstance();
	    AggregatedResource ar = null;

	    Date enddate = null;

	    while (itr.hasNext()) {
	        CaptureSearchResult cur = itr.next();
	        //I am not deduping urls (by digest) for the rdf serialization running out of time, extra efforts for me now ;)

	        String replayUrl = uriC.makeReplayURI(cur.getCaptureTimestamp(), 
	        		cur.getOriginalUrl());

	        URI replayURI = new URI(replayUrl);

	        String digest = cur.getDigest();
	        if (previos_digest == null) {
	            previos_digest = digest;
	        }

	        ar = aggregation.createAggregatedResource(replayURI);
	        ar.createTriple(purlPredicate, cur.getMimeType());

	        Predicate mementoForPredicate = new Predicate();
	        mementoForPredicate.setURI(MEMENTO_FOR_TERM_URI);
	        ar.createTriple(mementoForPredicate, requestURI);
	        ar.createTriple(rdfPredicate, MEMENTO_TERM_URI);

	        Date startdate = cur.getDuplicateDigestStoredDate();
	        enddate = cur.getCaptureDate();

	        // Adding blanc node
	        Triple triple = new TripleJena();
	        triple.initialise(replayURI);
	        Predicate observedOverPredicate = new Predicate();
	        observedOverPredicate.setURI(MEMENTO_OBSERVED_OVER_TERM_URI);

	        String uuidStr = URN_UUID + UUID.randomUUID().toString();
	        URI uuidURI = new URI(uuidStr);

	        triple.relate(observedOverPredicate, uuidURI);
	        Triple tr = new TripleJena();
	        tr.initialise(uuidURI);

	        tr.relate(rdfPredicate, MEMENTO_PERIOD_TERM_URI);

	        //period difined by [ [ interval [ date first digest recorded  and date of next digest recorded [ 

//	        String start = null;
	        Triple trd = new TripleJena();
	        trd.initialise(uuidURI);

	        if (startdate != null) {

	            cal.setTime(startdate);
	            trd.relate(startPredicate, cal);
//	            start = linkFormatter.format(startdate);
	        } else {
	            cal.setTime(enddate);
	            // is this supposed to be endPredicate??
	            trd.relate(startPredicate, cal);
//	            start = linkFormatter.format(enddate);
	        }

	        ar.addTriple(triple);
	        ar.addTriple(tr);
	        ar.addTriple(trd);

	        if (!digest.equals("previos_digest")) {

	            Iterator<String> it = previos_blancs.iterator();
	            while (it.hasNext()) {
	                String blanc_ = (String) it.next();
	                Triple tre = new TripleJena();
	                tre.initialise(new URI(blanc_));

	                cal.setTime(enddate);
	                tre.relate(endPredicate, cal);
	                ar.addTriple(tre);
	            }

	            previos_blancs.clear();
	            previos_digest = digest;
	        }

	        previos_blancs.add(uuidStr);

	    }

	    Iterator<String> it = previos_blancs.iterator();
	    while (it.hasNext()) {
	        String blanc_ = (String) it.next();
	        Triple tre = new TripleJena();
	        tre.initialise(new URI(blanc_));

	        cal.setTime(now); //or date of archive stop archiving
	        tre.relate(endPredicate, cal);

	        ar.addTriple(tre);
	    }



	    ORESerialiser serial = 
	    	ORESerialiserFactory.getInstance(MementoConstants.RDF_XML_SERIALIZER);
	    response.setContentType(MementoConstants.APPLICATION_RDF_XML_FORMAT);
	    //else if (format.equals("atom")) {
	    //  serial = ORESerialiserFactory.getInstance("ATOM-1.0");
	    //}
	    //else if (format.equals ("html")) {
	    //  serial = ORESerialiserFactory.getInstance("RDFa");
	    //}
	    //removed n3 because serialization of the date to the String type
	    //else if (format.equals("n3")) {
	    //serial = ORESerialiserFactory.getInstance("N3");

	    //response.setContentType("text/n3");
	    //}
	    if (serial != null) {
	        ResourceMapDocument doc = serial.serialise(resourceMap);
	        // TODO: this could get really big. Any way to stream the data out
	        //       so we don't need another copy beyond the ResourceMap, 
	        //       and other helper objects?

	        String serialisation = doc.toString();
	            //bug in jena? did not serialise date to date type but to string type // stupid fix will need investigate it 
	            serialisation = serialisation
	                    .replaceAll(
	                            "end rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string",
	                            "end rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime");
	            serialisation = serialisation
	                    .replaceAll(
	                            "start rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string",
	                            "start rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime");
	        PrintWriter pw = response.getWriter();
	        pw.print(serialisation);
	        pw.flush();
	    }
	}
	*/
}
