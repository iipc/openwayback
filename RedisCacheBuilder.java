package org.archive.wayback.accesscontrol.robotstxt;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.archive.extract.ExtractingResourceFactoryMapper;
import org.archive.extract.ExtractingResourceProducer;
import org.archive.extract.ProducerUtils;
import org.archive.extract.ResourceFactoryMapper;
import org.archive.format.http.HttpResponse;
import org.archive.format.json.JSONUtils;
import org.archive.resource.Resource;
import org.archive.resource.ResourceParseException;
import org.archive.resource.ResourceProducer;
import org.archive.resource.http.HTTPResponseResource;

import redis.clients.jedis.Jedis;

public class RedisCacheBuilder {
	
	int totalFiles = 0;
	int totalSuccessFiles = 0;
	
	int totalSize = 0;
	int totalSuccessSize = 0;
	
	int dupCount = 0;
	
	String redisHost;
	Jedis jedis;
	
	HashSet<String> urlDB = new HashSet<String>();

	public static void main(String args[])
	{
		new RedisCacheBuilder().run(args);
	}
	
	public void run(String[] args)
	{
	    Logger.getLogger("org.archive").setLevel(Level.WARNING);
	    
		String path = args[0];
		
		if (args.length > 1) {
			redisHost = args[1];
		}
		
		if (redisHost != null) {
			jedis = new Jedis(redisHost);
			jedis.connect();
		}		
		
		
		File rootFile = new File(path);
		
		if (rootFile.isDirectory()) {
			for (File file : rootFile.listFiles()) {
				if (!file.isDirectory() && file.getName().endsWith(".arc.gz")) {
					processFile(file.toString());
					System.out.println("ALL Files: " + totalFiles + " Size: " + totalSize);
					System.out.println("200 Files: " + totalSuccessFiles + " Size: " + totalSuccessSize);
				}
			}
		} else {
			processFile(path);	
		}
	    
		System.out.println("ALL Files: " + totalFiles + " Size: " + totalSize);
		System.out.println("200 Files: " + totalSuccessFiles + " Size: " + totalSuccessSize);
		System.out.println("Dups: " + dupCount);
	}
	
	public void processFile(String path)
	{
		try {
			System.out.println("Processing Path: " + path);
			
			ResourceProducer producer = ProducerUtils.getProducer(path);
			
		    ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
		    ExtractingResourceProducer exProducer = 
		    	new ExtractingResourceProducer(producer, mapper);
			
			Resource resource = null;
			
			resource = exProducer.getNext();
						
			while (resource != null) {
				
				resource = exProducer.getNext();
						
				if (resource instanceof HTTPResponseResource) {
					
					String url = JSONUtils.extractSingle(resource.getMetaData().getTopMetaData(), "Envelope.ARC-Header-Metadata.Target-URI");
					HTTPResponseResource httpResp = (HTTPResponseResource)resource;
					HttpResponse response = httpResp.getHttpResponse();
					int status = response.getMessage().getStatus();
					
					if (url.endsWith("/robots.txt")) {
						
						if (jedis != null) {
							if (jedis.exists(url)) {
								dupCount++;
								continue;								
							}
						} else {
							if (urlDB.contains(url)) {
								dupCount++;
								continue;
							}
							urlDB.add(url);
						}
						
						String contents = IOUtils.toString(httpResp.getHttpResponse().getInner(), "UTF-8");
						
						int size = contents.length();
						
						System.out.println("=== " + status + ": " + url + " " + size);
						
						totalSize += contents.length();
						totalFiles++;
						
						if (status == 200) {
							totalSuccessSize += contents.length();
							totalSuccessFiles++;
							if (jedis != null) {
								jedis.setex(url, 60 * 60 * 4, contents);
							}
						}
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ResourceParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
