<%@
page import="java.util.Date"
%><%@
page import="org.archive.wayback.util.graph.Graph"
%><%@
page import="org.archive.wayback.util.graph.GraphRenderer"
%><%@
page import="org.archive.wayback.util.graph.GraphEncoder"
%><%
Date now = new Date();
String arg = request.getParameter("graphdata");
if(arg == null) {
	arg = "(No Data specified)";
}
GraphRenderer r = new GraphRenderer();
response.setContentType(GraphRenderer.RENDERED_IMAGE_MIME);
Graph graph = GraphEncoder.decode(arg);
try {
	r.render(response.getOutputStream(),graph);
} catch(Exception e) {
	e.printStackTrace(System.out);
	//e.printStackTrace(response.getWriter());
}
%>