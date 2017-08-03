<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"
%><%@ page import="java.util.List"
%><%@ page import="java.util.ArrayList"
%><%@ page import="java.util.Calendar"
%><%@ page import="java.util.Date"
%><%@ page import="java.util.Iterator"
%><%@ page import="org.archive.wayback.ResultURIConverter"
%><%@ page import="org.archive.wayback.WaybackConstants"
%><%@ page import="org.archive.wayback.core.CaptureSearchResult"
%><%@ page import="org.archive.wayback.core.CaptureSearchResults"
%><%@ page import="org.archive.wayback.core.UIResults"
%><%@ page import="org.archive.wayback.core.WaybackRequest"
%><%@ page import="org.archive.wayback.partition.BubbleCalendarData"
%><%@ page import="org.archive.wayback.util.Timestamp"
%><%@ page import="org.archive.wayback.util.partition.Partition"
%><%@ page import="org.archive.wayback.util.StringFormatter"
%><%
UIResults results = UIResults.extractCaptureQuery(request);

StringFormatter fmt = results.getWbRequest().getFormatter();

ResultURIConverter uriConverter = results.getURIConverter();

// deployment-specific URL prefixes
String staticPrefix = results.getStaticPrefix();
String queryPrefix = results.getQueryPrefix();
String replayPrefix = results.getReplayPrefix();

//deployment-specific address for the graph generator:
String graphJspPrefix = results.getContextConfig("graphJspPrefix");
if(graphJspPrefix == null) {
	graphJspPrefix = queryPrefix;
}

// graph size "constants": These are currently baked-in to the JS logic...
int imgWidth = 0;
int imgHeight = 75;
int yearWidth = 60;
int monthWidth = 5;
int startYear = Timestamp.getStartYear();

imgWidth = yearWidth * (Calendar.getInstance().get(Calendar.YEAR) - startYear + 1);

BubbleCalendarData data = new BubbleCalendarData(results);

String yearEncoded = data.getYearsGraphString(imgWidth,imgHeight);
String yearImgUrl = graphJspPrefix + "jsp/graph.jsp?nomonth=1&graphdata=" + yearEncoded;

// a Calendar object for doing days-in-week, day-of-week,days-in-month math:
Calendar cal = BubbleCalendarData.getUTCCalendar();

%><!doctype html>
<html>
	<head>
		<meta http-equiv="content-type" content="text/html; charset=utf-8" />
        <title><%= fmt.format("UIGlobal.pageTitle") %></title>
<jsp:include page="/WEB-INF/template/CookieJS.jsp" flush="true" />
<link rel="stylesheet" href="<%= staticPrefix %>css/styles.css" type="text/css"/>
<link rel="stylesheet" href="<%= staticPrefix %>css/jquery.mCustomScrollbar.css" type="text/css" />

<script type="text/javascript" src="<%= staticPrefix %>js/jquery-1.4.2.min.js"></script>
<script type="text/javascript" src="<%= staticPrefix %>js/excanvas.compiled.js"></script>
<script type="text/javascript" src="<%= staticPrefix %>js/jquery.bt.min.js" charset="utf-8"></script>
<script type="text/javascript" src="<%= staticPrefix %>js/jquery.hoverintent.min.js" charset="utf-8"></script>
<script type="text/javascript" src="<%= staticPrefix %>js/graph-calc.js" ></script>
<script src="<%= staticPrefix %>js/jquery.mCustomScrollbar.concat.min.js" charset="utf-8"></script>
<!-- More ugly JS to manage the highlight over the graph -->
<script type="text/javascript">


var firstDate = <%= data.dataStartMSSE %>;
var lastDate = <%= data.dataEndMSSE %>;
var wbPrefix = "<%= replayPrefix %>";
var wbCurrentUrl = "<%= data.searchUrlForJS %>";

var curYear = <%= data.yearNum - startYear %>;
var curMonth = -1;
var yearCount = 15;
var firstYear = <%= startYear %>;
var startYear = <%= data.yearNum - startYear %>;
var imgWidth = <%= imgWidth %>;
var yearImgWidth = <%= yearWidth %>;
var monthImgWidth = <%= monthWidth %>;
var trackerVal = "none";

function showTrackers(val) {
	if(val == trackerVal) {
		return;
	}

    document.getElementById("wbMouseTrackYearImg").style.display = val;
    trackerVal = val;
}
function getElementX2(obj) {
	var thing = jQuery(obj);
	if((thing == undefined) 
			|| (typeof thing == "undefined") 
			|| (typeof thing.offset == "undefined")) {
		return getElementX(obj);
	}
	return Math.round(thing.offset().left);
}
function setActiveYear(year) {
    if(curYear != year) {
        var yrOff = year * yearImgWidth;
        document.getElementById("wbMouseTrackYearImg").style.left = yrOff + "px";
        if(curYear != -1) {
        	document.getElementById("highlight-"+curYear).setAttribute("class","inactiveHighlight");
        }
        document.getElementById("highlight-"+year).setAttribute("class","activeHighlight");
        curYear = year;
    }
}
function trackMouseMove(event,element) {

    var eventX = getEventX(event);
    var elementX = getElementX2(element);
    var xOff = eventX - elementX;
	if(xOff < 0) {
		xOff = 0;
	} else if(xOff > imgWidth) {
		xOff = imgWidth;
	}
    var monthOff = xOff % yearImgWidth;

    var year = Math.floor(xOff / yearImgWidth);
	var yearStart = year * yearImgWidth;
    var monthOfYear = Math.floor(monthOff / monthImgWidth);
    if(monthOfYear > 11) {
        monthOfYear = 11;
    }
    var month = (year * 12) + monthOfYear;
    var day = 1;
	if(monthOff % 2 == 1) {
		day = 15;
	}
	var dateString = 
		zeroPad(year + firstYear) + 
		zeroPad(monthOfYear+1,2) +
		zeroPad(day,2) + "000000";

	var url = "<%= queryPrefix %>" + dateString + '*/' +  wbCurrentUrl;
	document.getElementById('wm-graph-anchor').href = url;
	setActiveYear(year);
}
</script>

<script type="text/javascript">
$().ready(function(){
    $(".date").each(function(i){
        var actualsize = $(this).find(".hidden").text();
        var size = actualsize * 12;
        var offset = size / 2;
        if (actualsize == 1) {size = 30, offset = 15;}
        else if (actualsize == 2) {size = 40, offset = 20;}
        else if (actualsize == 3) {size = 50, offset = 25;}
        else if (actualsize == 4) {size = 60, offset = 30;}
        else if (actualsize == 5) {size = 70, offset = 35;}
        else if (actualsize == 6) {size = 80, offset = 40;}
        else if (actualsize == 7) {size = 90, offset = 45;}
        else if (actualsize == 8) {size = 100, offset = 50;}
        else if (actualsize == 9) {size = 110, offset = 55;}
        else if (actualsize >= 10) {size = 120, offset = 60;}
        $(this).find("img").attr("src","<%= staticPrefix %>images/blueblob-dk.png");
        $(this).find(".measure").css({'width':+size+'px','height':+size+'px','top':'-'+offset+'px','left':'-'+offset+'px'});
    });
    $(".day a").each(function(i){
        var dateClass = $(this).attr("class");
        var dateId = "#"+dateClass;
        $(this).hover(
            function(){$(dateId).removeClass("opacity20");},
            function(){$(dateId).addClass("opacity20");}
        );
    });
    $(".tooltip").bt({
        positions: ['top','right','left','bottom'],
        trigger: ['focus mouseover', 'click'],
        contentSelector: "$(this).find('.pop').html()",
        padding: 0, 
        width: '130px',
        spikeGirth: 8, 
        spikeLength: 8,
        overlap: 0,
        cornerRadius: 5,
        fill: '#efefef',
        strokeWidth: 1,
        strokeStyle: '#efefef',
        shadow: true, 
        shadowColor: '#333',
        shadowBlur: 5,
        shadowOffsetX: 0,
        shadowOffsetY: 0, 
        noShadowOpts: {strokeStyle:'#ccc'},
        clickAnywhereToClose: true,
        closeWhenOthersOpen: true,
        windowMargin: 30,
        cssStyles: {
            fontSize: '12px',
            fontFamily: '"Arial","Helvetica Neue","Helvetica",sans-serif',
            lineHeight: 'normal',
            padding: '10px',
            color: '#333'
        }
    });

    var yrCount = $(".wbChartThisContainer").size();
    var yrTotal = <%= yearWidth %> * yrCount;
    var yrPad = (930 - yrTotal) / 2;
    $("#wbChartThis").css("padding-left",yrPad+"px");
});
</script>
</head>
<body>
<div id="position">


    <div id="wbSearch">
    
        <div id="logo">
            <a href="<%= queryPrefix %>"><img src="<%= staticPrefix %>images/OpenWayback-banner.png" alt="logo: OpenWayback" /></a>
        </div>

        <div id="form">
        
            <form name="form1" method="get" action="<%= queryPrefix %>query">
			<input type="hidden" name="<%= WaybackRequest.REQUEST_TYPE %>" value="<%= WaybackRequest.REQUEST_CAPTURE_QUERY %>">
                        <input type="text" name="<%= WaybackRequest.REQUEST_URL %>" value="<%= data.searchUrlForHTML %>" size="40" maxlength="256">
            <input type="submit" name="Submit" value="<%= fmt.format("UIGlobal.urlSearchButton") %>"/>
            </form>
    
            <div id="wbMeta">
                <p class="wbThis"><%= fmt.format("BubbleCalendar.crawledInfo",
                                            data.searchUrlForHTML, 
                                            data.numResults,
                                            data.firstResultReplayUrl,
                                            data.firstResultDate,
                                            data.numYearResults,
                                            data.yearNum) %></p>
                <p class="wbNote"><%= fmt.format("BubbleCalendar.crawledInfoDuplicate") %><a href="<%= fmt.format("UIGlobal.helpUrl") %>"><%= fmt.format("BubbleCalendar.documentation") %></a></p>
            </div>
        </div>
        
    </div>
    
    <div class="clearfix"></div>

    <div id="wbChart" onmouseout="showTrackers('none'); setActiveYear(startYear);" style="width: 963px; height: 118px;">
    
  <div id="wbChartThis">
        <a style="position:relative; white-space:nowrap; width:<%= imgWidth %>px;height:<%= imgHeight %>px;" href="<%= queryPrefix %>" id="wm-graph-anchor">
        <div id="wm-ipp-sparkline" style="position:relative; white-space:nowrap; width:<%= imgWidth %>px;height:<%= imgHeight %>px;background: #f3f3f3 -moz-linear-gradient(top,#ffffff,#f3f3f3);background: #f3f3f3 -webkit-gradient(linear, left top, left bottom, from(#fff), to(#f3f3f3), color-stop(1.0, #f3f3f3));background-color: #f3f3f3;filter: progid:DXImageTransform.Microsoft.Gradient(enabled='true',startColorstr=#FFFFFFFF, endColorstr=#FFF3F3F3);cursor:pointer;border: 1px solid #ccc;border-left:none;" title="<%= fmt.format("ToolBar.sparklineTitle") %>">
			<img id="sparklineImgId" style="position:absolute;z-index:9012;top:0;left:0;"
				onmouseover="showTrackers('inline');" 
				
				onmousemove="trackMouseMove(event,this)"
				alt="sparklines"
				width="<%= imgWidth %>"
				height="<%= imgHeight %>"
				border="0"
				src="<%= yearImgUrl %>"/>
			<img id="wbMouseTrackYearImg" 
				style="display:none; position:absolute; z-index:9010;"
				width="<%= yearWidth %>" 
				height="<%= imgHeight %>"
				border="0"
				src="<%= staticPrefix %>images/yellow-pixel.png"/>
        </div>
        </a>
        	<%
        	for(int i = startYear; i <= Calendar.getInstance().get(Calendar.YEAR); i++) {
        		String curClass = "inactiveHighlight";
        		if(data.yearNum == i) {
            		curClass = "activeHighlight";
        		}
        	%>
	            <div class="wbChartThisContainer">
	                <a style="text-decoration: none;" href="<%= queryPrefix + i + "0201000000*/" + data.searchUrlForHTML %>">
	                
	                	<div id="highlight-<%= i - startYear %>"
						onmouseover="showTrackers('inline'); setActiveYear(<%= i - startYear %>)" 
	                	class="<%= curClass %>"><%= i %></div>
	                </a>
	            </div>
            <%
        	}
            %>
  </div>
</div>
  
<script>
  
    var x = sessionStorage.getItem("scrollbarX");
    
    (function($){
        $("#wbChart").mCustomScrollbar({
            axis: "x",
            theme: "rounded-dots-dark",
            autoExpandScrollbar: true,
            scrollButtons: {enable: true},
            keyboard: {enable: true},
            documentTouchScroll: true,

            callbacks:{
                onUpdate:function(){
                    $("#mCSB_1_container").css("left", x);
                },
                whileScrolling:function()
                {
                    leftAmount = $("#mCSB_1_container").css("left");
                    sessionStorage.setItem("scrollbarX", leftAmount);
                }
            }
        });
    })(jQuery);
</script>  

<div class="clearfix"></div>

<div id="wbCalendar">
    
  <div id="calUnder" class="calPosition">

    


<%
// draw 12 months, 0-11 (0=Jan, 11=Dec)
for(int moy = 0; moy < 12; moy++) {
	Partition<Partition<CaptureSearchResult>> curMonth = data.monthsByDay.get(moy);
	List<Partition<CaptureSearchResult>> monthDays = curMonth.list();
%>
    <div class="month" id="<%= data.yearNum %>-<%= moy %>">
	    <table>
	
	       <thead>
	           <tr>
	               <th colspan="7"><span class="label"></span></th>
	           </tr>
	       </thead>
	       <tbody>
	           <tr>
<%
		cal.setTime(curMonth.getStart());
		int skipDays = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		// skip until the 1st:
		for(int i = 0; i < skipDays; i++) {
			%><td><div class="date"></div></td><%
		}
		int dow = skipDays;
		int dom;
		for(dom = 0; dom < daysInMonth; dom++) {


			int count = monthDays.get(dom).count();
			if(count > 0) {
				// one or more captures in this day:
				CaptureSearchResult firstCaptureInDay = 
					monthDays.get(dom).list().get(0);
				String replayUrl = uriConverter.makeReplayURI(
							firstCaptureInDay.getCaptureTimestamp(),
							firstCaptureInDay.getOriginalUrl());
				Date firstCaptureInDayDate = firstCaptureInDay.getCaptureDate();
				String safeUrl = fmt.escapeHtml(replayUrl);		
				%><td>
                    <div class="date">
                        <div class="position">
                           <div class="hidden"><%= count %></div>
                           <div class="measure opacity20" id="<%= fmt.format("{0,date,MMM-d-yyyy}",firstCaptureInDayDate) %>"><img width="100%" height="100%"/></div>
                        </div>
                    </div>
				</td><%

			} else {
				// zero captures in this day:
				%><td>
	                <div class="date"></div>
				</td><%
				
			}


			if(((dom+skipDays+1) % 7) == 0) {
				// end of the week, start a new tr:
				%></tr><tr><%
			}
		}
		// fill in blank days until the end of the current week:
		while(((dom+skipDays) % 7) != 0) {
			%><td></td><%
			dom++;
		}
%>
			   </tr>
			</tbody>
		</table>    
      </div>
    
<%
}
%>
  </div>
  <div id="calOver" class="calPosition">
<%

for(int moy = 0; moy < 12; moy++) {
	Partition<Partition<CaptureSearchResult>> curMonth = data.monthsByDay.get(moy);
	List<Partition<CaptureSearchResult>> monthDays = curMonth.list();
%>
    <div class="month" id="<%= data.yearNum %>-<%= moy %>">
	    <table>
	
	       <thead>
	           <tr>
	               <th colspan="7"><span class="label"><%= fmt.format("{0,date,MMM}",curMonth.getStart()) %></span></th>
	           </tr>
	       </thead>
	       <tbody>
	           <tr>
<%
		cal.setTime(curMonth.getStart());
		int skipDays = cal.get(Calendar.DAY_OF_WEEK) - 1;
		int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		// skip until the 1st:
		for(int i = 0; i < skipDays; i++) {
			%><td><div class="date"></div></td><%
		}
		int dow = skipDays;
		int dom;
		for(dom = 0; dom < daysInMonth; dom++) {


			int count = monthDays.get(dom).count();
			
			if(count > 0) {
				// one or more captures in this day:
				CaptureSearchResult firstCaptureInDay =
					monthDays.get(dom).list().get(0);
				String replayUrl = uriConverter.makeReplayURI(
						firstCaptureInDay.getCaptureTimestamp(),
						firstCaptureInDay.getOriginalUrl());
				Date firstCaptureInDayDate = firstCaptureInDay.getCaptureDate();
				String safeUrl = fmt.escapeHtml(replayUrl);

				%><td>
                    <div class="date tooltip">
                        <div class="pop">
                            <h3><%= fmt.format("{0,date,MMMMM d, yyyy}",firstCaptureInDayDate) %></h3>
                            <p><%= count %> snapshots</p>
                            <div style="overflow: auto; max-height: 50vh;">
                                <ul>
							<%
							Iterator<CaptureSearchResult> dayItr = 
								monthDays.get(dom).iterator();
							while(dayItr.hasNext()) {
								CaptureSearchResult c = dayItr.next();
								String replayUrl2 = uriConverter.makeReplayURI(
										c.getCaptureTimestamp(),c.getOriginalUrl());
								String safeUrl2 = fmt.escapeHtml(replayUrl2);
								%>
								<li><a href="<%= safeUrl2 %>"><%= fmt.format("{0,date,HH:mm:ss}",c.getCaptureDate()) %></a></li>
								<%
							}
							%>
                                </ul>
                            </div>
                        </div>
                        <div class="day">

                            <a href="<%= safeUrl %>" title="<%= count %> snapshots" class="<%= fmt.format("{0,date,MMM-d-yyyy}",firstCaptureInDayDate) %>"><%= dom + 1 %></a>
                        </div>
                    </div>
			      </td><%

			} else {
				// zero captures in this day:
				%><td>
	                <div class="date">
	    	            <div class="day"><span><%= dom + 1 %></span></div>
		            </div>
				</td><%
				
			}


			if(((dom+skipDays+1) % 7) == 0) {
				// end of the week, start a new tr:
				%></tr><tr><%
			}
		}
		// fill in blank days until the end of the current week:
		while(((dom+skipDays) % 7) != 0) {
			%><td></td><%
			dom++;
		}
%>
			   </tr>
			</tbody>
		</table>    
      </div>
<%
}
%>
    </div>
  </div>
  <div id="wbCalNote">
    <h2><%= fmt.format("BubbleCalendar.wbCalNoteTitle") %></h2>
    <p><%= fmt.format("BubbleCalendar.wbCalNote", data.searchUrlForHTML, fmt.format("UIGlobal.helpUrl")) %></p>
  </div>
</div>
  
<script>
    var body = document.body;
    var html = document.documentElement;
    body.scrollTop = sessionStorage.getItem("top");

    window.onscroll = function() {setTop()};

    function setTop(){
        sessionStorage.setItem("top", body.scrollTop);
    }
</script>
    
<jsp:include page="/WEB-INF/template/UI-footer.jsp" flush="true" />
