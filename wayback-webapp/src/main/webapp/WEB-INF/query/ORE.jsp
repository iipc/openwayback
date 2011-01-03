<%@ page import="java.util.Date"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.core.CaptureSearchResults"
%><%@ page import="org.archive.wayback.core.CaptureSearchResult"
%><%@ page import="org.archive.wayback.ResultURIConverter"
%><%@ page import="java.text.SimpleDateFormat"
%><%@ page import="org.archive.wayback.util.Timestamp"
%><%@ page import="java.util.Iterator"
%><%@ page import="java.util.List"
%><%@ page import="java.util.ArrayList"
%><%@ page import="org.dspace.foresite.Aggregation"
%><%@ page import="org.dspace.foresite.ResourceMap"
%><%@ page import="org.dspace.foresite.Agent"
%><%@ page import="org.dspace.foresite.OREFactory"
%><%@ page import="org.dspace.foresite.AggregatedResource"
%><%@ page import="org.dspace.foresite.ORESerialiser"
%><%@ page import="org.dspace.foresite.ORESerialiserFactory"
%><%@ page import="org.dspace.foresite.ResourceMapDocument"
%><%@ page import="java.io.PrintWriter"
%><%@ page import="java.net.URI"
%><%@ page import="org.dspace.foresite.Predicate"
%><%@ page import="org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter"
%><%@ page import="org.dspace.foresite.Triple"
%><%@ page import="org.dspace.foresite.jena.TripleJena"
%><%@ page import="java.util.UUID"
%><%@ page import="java.util.TimeZone"
%><%@ page import="java.util.Calendar"
%><%
	UIResults results = UIResults.extractCaptureQuery(request);//nuzno potom perepisat'
	SimpleDateFormat httpformatterl = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss z");
	TimeZone tzo = TimeZone.getTimeZone("GMT");
	httpformatterl.setTimeZone(tzo);

	WaybackRequest wbRequest = results.getWbRequest();
	CaptureSearchResults cResults = results.getCaptureResults();
	CaptureSearchResult res = cResults.getClosest();

    String replayPrefix = wbRequest.getAccessPoint().getReplayPrefix();
    String queryPrefix = wbRequest.getAccessPoint().getQueryPrefix();
	String u = wbRequest.getRequestUrl();
	String agguri = replayPrefix + "timebundle/" + u;
	String format = wbRequest.get("format");
	if(format == null) {
		format = "rdf";
	}
	Aggregation agg = OREFactory.createAggregation(new URI(agguri));
	ResourceMap rem = agg.createResourceMap(new URI(queryPrefix
			+ "timemap/" + format + "/" + u));

	Date now = new Date();

	rem.setCreated(now);
	Predicate pr_type = new Predicate();
	pr_type.setURI(new URI(
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));

	rem.setModified(now);
	rem.createTriple(pr_type, new URI(
			"http://www.mementoweb.org/terms/tb/TimeMap"));
	Agent creator = OREFactory.createAgent();
	creator.addName("Foresite Toolkit (Java)");

	rem.addCreator(creator);
	agg.addTitle("Memento Time Bundle for " + u);

	Iterator<CaptureSearchResult> itr = cResults.iterator();
	SimpleDateFormat formatterk = new SimpleDateFormat("yyyyMMddHHmmss");
	formatterk.setTimeZone(tzo);
	Date f = cResults.getFirstResultDate();
	Date l = cResults.getLastResultDate();

	String ArchiveInterval = formatterk.format(f) + " - "
			+ formatterk.format(l);

	agg.addType(new URI("http://www.mementoweb.org/terms/tb/TimeBundle"));
	//include original into aggregation

	AggregatedResource ar_o = agg.createAggregatedResource(new URI(u));
	ar_o.createTriple(pr_type, new URI(
			"http://www.mementoweb.org/terms/tb/OriginalResource"));
	//include timegate into aggregation
	AggregatedResource ar_tg = agg.createAggregatedResource(new URI(
			replayPrefix + "timegate/" + u));

	Predicate pr_format = new Predicate();
	pr_format.setURI(new URI("http://purl.org/dc/elements/1.1/format"));
	ar_tg.createTriple(pr_format, new URI(u));
	ar_tg.createTriple(pr_type, new URI(
			"http://www.mementoweb.org/terms/tb/TimeGate"));

	String previos_digest = null;
	List<String> previos_blancs = new ArrayList<String>();

	Predicate pr = new Predicate();
	pr.setURI(new URI("http://www.mementoweb.org/terms/tb/start"));
	Predicate pre = new Predicate();
	pre.setURI(new URI("http://www.mementoweb.org/terms/tb/end"));
	Calendar cal = Calendar.getInstance();
	AggregatedResource ar = null;

	Date enddate = null;

	// String buffer for special link serialization format
	StringBuffer linkbf = new StringBuffer();

	linkbf.append("<" + u + ">;rel=\"original\"\n");
	linkbf.append(",<" + agguri + ">;rel=\"timebundle\"\n");
	linkbf.append(",<" + replayPrefix
			+ "timegate/" + u + ">;rel=\"timegate\"\n");
	linkbf.append(",<" + queryPrefix + "timemap/" + format + "/" + u
			+ ">;rel=\"timemap\";type=\"application/link-format\"\n");

	String firstmemento = null;
	int count = 0;
	while (itr.hasNext()) {
		CaptureSearchResult cur = itr.next();
		//I am not deduping urls (by digest) for the rdf serialization running out of time, extra efforts for me now ;)

		String resurl = replayPrefix
				+ formatterk.format(cur.getCaptureDate()) + "/" + u;

		String digest = cur.getDigest();
		if (previos_digest == null) {
			previos_digest = digest;
		}

		ar = agg.createAggregatedResource(new URI(resurl));
		ar.createTriple(pr_format, cur.getMimeType());

		Predicate pr_1 = new Predicate();
		pr_1.setURI(new URI(
				"http://www.mementoweb.org/terms/tb/mementoFor"));
		ar.createTriple(pr_1, new URI(u));
		ar.createTriple(pr_type, new URI(
				"http://www.mementoweb.org/terms/tb/Memento"));

		Date startdate = cur.getDuplicateDigestStoredDate();
		enddate = cur.getCaptureDate();

		// serialiase it in links format only for unique  digest

		if (startdate == null) {
			if (firstmemento == null) {
				linkbf.append(",<" + resurl
						+ ">;rel=\"first memento\";datetime=\""
						+ httpformatterl.format(enddate) + "\"\n");
				firstmemento = "firstmemento";

			} else {
				linkbf.append(",<" + resurl
						+ ">;rel=\"memento\";datetime=\""
						+ httpformatterl.format(enddate) + "\"\n");
				count = count + 1;
			}
		}

		// Adding blanc node
		Triple triple = new TripleJena();
		triple.initialise(new URI(resurl));
		Predicate pred = new Predicate();
		UUID a = UUID.randomUUID();
		String blanc = "urn:uuid:" + a.toString();

		pred.setURI(new URI(
				"http://www.mementoweb.org/terms/tb/observedOver"));
		triple.relate(pred, new URI(blanc));
		Triple tr = new TripleJena();
		tr.initialise(new URI(blanc));

		tr.relate(pr_type, new URI(
				"http://www.mementoweb.org/terms/tb/Period"));

		//period difined by [ [ interval [ date first digest recorded  and date of next digest recorded [ 

		String start = null;
		Triple trd = new TripleJena();
		trd.initialise(new URI(blanc));

		if (startdate != null) {

			cal.setTime(startdate);
			trd.relate(pr, cal);
			start = httpformatterl.format(startdate);
		} else {
			cal.setTime(enddate);
			trd.relate(pr, cal);
			start = httpformatterl.format(enddate);
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
				tre.relate(pre, cal);
				ar.addTriple(tre);
			}

			previos_blancs.clear();
			previos_digest = digest;
		}

		previos_blancs.add(blanc);

	}

	Iterator it = previos_blancs.iterator();
	while (it.hasNext()) {
		String blanc_ = (String) it.next();
		Triple tre = new TripleJena();
		tre.initialise(new URI(blanc_));

		cal.setTime(now); //or date of archive stop archiving
		tre.relate(pre, cal);

		ar.addTriple(tre);
	}

	if (count > 0) {
		int m_index = linkbf.lastIndexOf("\"memento\"");
		linkbf.insert(m_index + 1, "last ");
	}

	ORESerialiser serial = null;
	if (format.equals("rdf")) {
		serial = ORESerialiserFactory.getInstance("RDF/XML");
		response.setContentType("application/rdf+xml");
	}
	//else if (format.equals("atom")) {
	//	serial = ORESerialiserFactory.getInstance("ATOM-1.0");
	//}
	//else if (format.equals ("html")) {
	//	serial = ORESerialiserFactory.getInstance("RDFa");
	//}
	//removed n3 because serialization of the date to the String type
	//else if (format.equals("n3")) {
	//serial = ORESerialiserFactory.getInstance("N3");

	//response.setContentType("text/n3");
	//}
	else if (format.equals("link")) {
		PrintWriter pw = response.getWriter();

		response.setContentType("application/link-format");
		pw.print(linkbf.toString());
		pw.flush();

	} else {
		// TODO: this should be handled in TimeBundleParser to allow
		//       usual Exception rendering to happen.
		response.sendError(404, "Unknown TimeMap serialization");
	}
	if (serial != null) {
		ResourceMapDocument doc = serial.serialise(rem);
		// TODO: this could get really big. Any way to stream the data out
		//       so we don't need another copy beyond the ResourceMap, 
		//       and other helper objects?
		String serialisation = doc.toString();
		if (format.equals("rdf")) {
			//bug in jena? did not serialise date to date type but to string type // stupid fix will need investigate it 
			serialisation = serialisation
					.replaceAll(
							"end rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string",
							"end rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime");
			serialisation = serialisation
					.replaceAll(
							"start rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string",
							"start rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime");
		}
		PrintWriter pw = response.getWriter();
		pw.print(serialisation);
		pw.flush();
	}
%>