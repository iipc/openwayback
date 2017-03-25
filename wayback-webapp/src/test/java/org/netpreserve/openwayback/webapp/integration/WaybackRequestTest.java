package org.netpreserve.openwayback.webapp.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class WaybackRequestTest {
	
	@Before
	public void await() throws InterruptedException {
		// Give the newly booted OpenWayback instance a few seconds to index the
		// test warc(s):
		System.out.println("Pausing before testing...");
		Thread.sleep(10 * 1000);
	}

	/**
	 * 
	 * @throws IOException
	 */
	@Test
	public void testInterject() throws IOException {
		this.checkUrlContentType(
				"http://localhost:18080/wayback/20120118143132/http://en.wikipedia.org/wiki/Main_Page",
				"text/html; charset=UTF-8");
		this.checkUrlContentType(
				"http://localhost:18080/wayback/20120118143132/http://en.wikipedia.org/robots.txt",
				"text/plain; charset=UTF-8");
	}

	/**
	 * 
	 * @param url
	 * @param expectedType
	 * @throws IOException
	 */
	private void checkUrlContentType( String url, String expectedType ) throws IOException {
		CloseableHttpClient httpclient = HttpClients.createSystem();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse res = httpclient.execute(httpGet);
		// Check response exists:
		Assert.assertNotNull("Response for "+url+" should not be NULL.", res);
		// Check the Content-Type:
		assertNotNull("Content-Type should not be NULL for "+url, res.getFirstHeader("Content-Type"));
		assertEquals( expectedType, res.getFirstHeader("Content-Type").getValue() );
		// Download the content:
		byte[] out = IOUtils.toByteArray(res.getEntity().getContent());
		assertNotNull(out);
		// Clean up:
		res.close();
		httpclient.close();
	}

}
