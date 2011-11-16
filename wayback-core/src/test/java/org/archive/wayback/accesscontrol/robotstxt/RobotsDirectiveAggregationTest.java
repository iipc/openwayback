package org.archive.wayback.accesscontrol.robotstxt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import junit.framework.TestCase;

public class RobotsDirectiveAggregationTest extends TestCase {
	
	private String[] mapRobotUrls(String[] in ) {
		String res[] = new String[in.length];
		for(int i = 0; i < in.length; i++) {
			res[i] = "http://" + in[i] + "/robots.txt";
		}
		return res;
	}
	
	
	/**
	 * 
	 */
	public void testHostToRobotUrlStrings() {
		RobotsDirectiveAggregation f = new RobotsDirectiveAggregation();
		String test1[] = {"www.foo.com","foo.com"};
		compareListTo(f.hostToRobotUrlStrings("www.foo.com"),mapRobotUrls(test1));

		String test2[] = {"foo.com","www.foo.com"};
		compareListTo(f.hostToRobotUrlStrings("foo.com"),mapRobotUrls(test2));

		String test3[] = {"fool.foo.com","www.fool.foo.com"};
		compareListTo(f.hostToRobotUrlStrings("fool.foo.com"),mapRobotUrls(test3));

		String test4[] = {"www4.foo.com","www.foo.com","foo.com"};
		compareListTo(f.hostToRobotUrlStrings("www4.foo.com"),mapRobotUrls(test4));

		String test5[] = {"www4w.foo.com"};
		compareListTo(f.hostToRobotUrlStrings("www4w.foo.com"),mapRobotUrls(test5));
		
		String test6[] = {"www.www.foo.com","www.foo.com"};
		compareListTo(f.hostToRobotUrlStrings("www.www.foo.com"),mapRobotUrls(test6));
	}
	private String strJoin(Iterable<String> i, char del) {
		StringBuilder sb  = new StringBuilder();
		boolean first = true;
		for(String s : i) {
			if(first) {
				first = false;
			} else {
				sb.append(del);
			}
			sb.append(s);
		}
		return sb.toString();
	}
	private List<String> sortA(String[] a) {
		Arrays.sort(a);
		return Lists.newArrayList(a);
	}
	private List<String> sortL(List<String> a) {
		String[] Empty = new String[0];
		String[] tmp;
		tmp = a.toArray(Empty);
		Arrays.sort(tmp);
		return Lists.newArrayList(tmp);
	}
	private void compareListTo(List<String> list, String strings[]) {
		
		boolean match = list.size() == strings.length;
		List<String> ls = sortL(list);
		List<String> ss = sortA(strings);
		if(match) {
			for(int i = 0; i < strings.length; i++) {
				if(!ls.get(i).equals(ss.get(i))) {
					match = false;
					break;
				}
			}
		}
		if(!match) {
			String a1 = strJoin(ls,',');
			String a2 = strJoin(ss,',');
			String msg = String.format("ArrayCMP (%s) != (%s)",a1,a2);
			assertTrue(msg,false);
		}
	}

	public void testInteraction() {
		RobotsDirectiveAggregation agg = new RobotsDirectiveAggregation();
		String test1[] = {"http://foo.com/robots.txt","http://www.foo.com/robots.txt"};
		compareListTo(agg.getMissingRobotUrls("foo.com"),test1);
		compareListTo(agg.getMissingRobotUrls("www.foo.com"),test1);
		agg.addDirectives("http://foo.com/robots.txt", new FixedRobotsDirectives(true));
		String test2[] = {"http://www.foo.com/robots.txt"};
		compareListTo(agg.getMissingRobotUrls("foo.com"),test2);
		assertFalse(agg.isBlocked("/foo"));

		agg.addDirectives("http://www.foo.com/robots.txt", new FixedRobotsDirectives(false));
		String test3[] = {};
		compareListTo(agg.getMissingRobotUrls("foo.com"),test3);
		assertTrue(agg.isBlocked("/foo"));
		
	}
}
