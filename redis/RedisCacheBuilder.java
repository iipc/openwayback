package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
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
	
	FileFilter fileFilter = FileFilterUtils.suffixFileFilter(".arc.gz");
	
	public static void main(String args[])
	{
		new RedisCacheBuilder().run(args);
	}
	
	protected void loadFromJedis()
	{
		try {
			totalSuccessFiles = Integer.valueOf(jedis.get("_totalSuccessFiles"));
			totalFiles = Integer.valueOf(jedis.get("_totalFiles"));
			totalSuccessSize = Integer.valueOf(jedis.get("_totalSuccessSize"));
			totalSize = Integer.valueOf(jedis.get("_totalSize"));
			dupCount = Integer.valueOf(jedis.get("_dupCount"));
		} catch (NumberFormatException nfe) {
			nfe.printStackTrace();
		}
	}
	
	protected void saveToJedis()
	{
		jedis.set("_totalSuccessFiles", String.valueOf(totalSuccessFiles));
		jedis.set("_totalFiles", String.valueOf(totalFiles));
		jedis.set("_totalSuccessSize", String.valueOf(totalSuccessSize));
		jedis.set("_totalSize", String.valueOf(totalSize));
		jedis.set("_dupCount", String.valueOf(dupCount));
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
			
			loadFromJedis();
		}
			
		File rootFile = new File(path);
		
		if (rootFile.isDirectory()) {
			for (File file : rootFile.listFiles(fileFilter)) {
				processFile(file.toString());
			}
		} else {
			processFile(path);	
		}
		
		if ((args.length > 2) && args[2].equals("-d")) {
			processNewFiles(path);
			return;
		}
	}
	
	protected void processNewFiles(String path)
	{
		FileAlterationObserver observer = new FileAlterationObserver(path, fileFilter);
		observer.addListener(new FileAlterationListenerAdaptor()
		{
			@Override
			public void onFileCreate(File file) {
				processFile(file.toString());
			}	
		});
		
		FileAlterationMonitor monitor = new FileAlterationMonitor(1000, observer);
		try {
			monitor.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processFile(String path)
	{
		if (jedis != null) {
			if (jedis.sismember("_files", path)) {
				System.err.println("Skipping Already Processed Path: " + path);
				return;
			}
			jedis.sadd("_files", path);
		}
		
		try {
			System.err.println("Processing Path: " + path);
			
			ResourceProducer producer = ProducerUtils.getProducer(path);
			
		    ResourceFactoryMapper mapper = new ExtractingResourceFactoryMapper();
		    ExtractingResourceProducer exProducer = 
		    	new ExtractingResourceProducer(producer, mapper);
			
			Resource resource = null;
			
			resource = exProducer.getNext(); // ARC File Info, skipping
						
			while (resource != null) {
				
				resource = exProducer.getNext();
						
				if (resource instanceof HTTPResponseResource) {
					
					String url = JSONUtils.extractSingle(resource.getMetaData().getTopMetaData(), "Envelope.ARC-Header-Metadata.Target-URI");
					HTTPResponseResource httpResp = (HTTPResponseResource)resource;
					HttpResponse response = httpResp.getHttpResponse();
					int status = response.getMessage().getStatus();
					
					if (url.endsWith("/robots.txt")) {
						
						if ((jedis != null) && (jedis.exists(url))) {
							dupCount++;
							continue;
						}
						
						String contents = IOUtils.toString(response.getInner(), "UTF-8");
						
						String contentType = response.getHeaders().getValue("Content-Type");
						
						int size = contents.length();
						
						System.out.println(size + " " + status + " " + url + " " + contentType);
						
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
		} finally {
			System.err.println("ALL Files: " + totalFiles + " Size: " + totalSize);
			System.err.println("200 Files: " + totalSuccessFiles + " Size: " + totalSuccessSize);
			
			if (jedis != null) {
				saveToJedis();
			}
		}
	}
}
