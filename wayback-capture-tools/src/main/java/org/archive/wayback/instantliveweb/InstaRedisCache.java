package org.archive.wayback.instantliveweb;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.hadoop.thirdparty.guava.common.collect.Sets;
import org.archive.format.cdx.CDXInputSource;
import org.archive.format.gzip.zipnum.LineBufferingIterator;
import org.archive.format.gzip.zipnum.ZipNumIndex;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.CloseableIteratorWrapper;
import org.archive.wayback.accesscontrol.robotstxt.redis.RedisConnectionManager;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourcestore.FlexResourceStore.SourceResolver;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class InstaRedisCache implements InstaPersistCache, SourceResolver {
	
	// Iterator which adds a prefix to each line
	public static class PrefixKeyIteratorWrapper extends CloseableIteratorWrapper<String>
	{
		protected String prefix;
		
		public PrefixKeyIteratorWrapper(Iterator<String> iter, String prefix) {
	        super(iter);
	        this.prefix = prefix;
        }
		
		@Override
        public String next() {
			return prefix + super.next();
        }
	}
	
	private final static String[] EMPTY_STRINGS = new String[0];
	private final static Set<String> EMPTY_SET = Sets.newIdentityHashSet();
	
	private final static String CDX_KEY = "c:";
	private final static String WARC_KEY = "w:";
	
	private final static String WARC_PATH = "path";
	
	protected RedisConnectionManager redisMgr;
	
	protected int cdxExpireSec = 0;
	protected int warcExpireSec = 0;
	protected int removeOlderThanSec = 0;
	
	protected boolean storePathIndex = true;
	
	protected String totalKey;
	protected CDXInputSource totalInput;
	
	@Override
	public boolean saveResult(CaptureSearchResult result)
	{
		Jedis jedis = null;
		boolean success = false;
		
		try {
			jedis = redisMgr.getJedisInstance();
			
			double score = result.getCaptureDate().getTime();
			
			String cdxLine = resultToCDXLine(result);
			
			jedis.zadd(CDX_KEY + result.getUrlKey(), score, cdxLine);
			
			if (storePathIndex) {
				String key = WARC_KEY + result.getFile();
				
				jedis.hset(key, WARC_PATH, result.getCustom(InstaLiveWebWarcWriter.FULL_PATH) + result.getFile());
				
				if (warcExpireSec > 0) {
					jedis.expire(WARC_KEY + key, warcExpireSec);
				}
			}
			
			success = true;
						
			if (cdxExpireSec > 0) {
				jedis.expire(CDX_KEY + result.getUrlKey(), cdxExpireSec);
			}
			
			if (removeOlderThanSec > 0) {
				long oldest = System.currentTimeMillis() - (removeOlderThanSec * 1000);
				jedis.zremrangeByScore(CDX_KEY + result.getUrlKey(), 0, oldest);
			}
			
			if (totalKey != null) {
				long totalLines = totalInput.getTotalLines();
				
				String strValue = jedis.get(totalKey);
				
				long currValue = NumberUtils.toLong(strValue, 0);
				
				if (totalLines > currValue) {				
					jedis.set(totalKey, String.valueOf(totalLines));
				}
			}

		} catch (JedisConnectionException jce) {
			redisMgr.returnBrokenJedis(jedis);
			jedis = null;
		} finally {
			redisMgr.returnJedisInstance(jedis);
		}
		
		return success;
	}
	
	protected Set<String> loadCdx(String urlkey)
	{
		Jedis jedis = null;
		Set<String> cdx = null;
		
		try {
			jedis = redisMgr.getJedisInstance();
			
			cdx = jedis.zrange(CDX_KEY + urlkey, 0, -1);
			
		} catch (JedisConnectionException jce) {
			redisMgr.returnBrokenJedis(jedis);
			jedis = null;
		} finally {
			redisMgr.returnJedisInstance(jedis);
		}
		
		return (cdx == null ? EMPTY_SET : cdx);
	}
	
	public CloseableIterator<String> loadCDXIterator(String urlkey, boolean reverse)
	{
		int space = urlkey.indexOf(' ');
		
		//TODO: support range by startDate?
		if (space > 0) {
			urlkey = urlkey.substring(0, space);
		}
		
		Set<String> cdx = loadCdx(urlkey);
		//CloseableIterator<String> iter = new CloseableIteratorWrapper<String>(cdx.iterator());
		CloseableIterator<String> iter = new PrefixKeyIteratorWrapper(cdx.iterator(), urlkey + " ");
		
		if (reverse) {
			iter = new LineBufferingIterator(iter, cdx.size(), true);
		}
		
		return iter;
	}
	
	// Not adding the first field to save space as its the key
	public static String resultToCDXLine(CaptureSearchResult result)
	{
		StringBuilder sb = new StringBuilder();
		//sb.append(' ');
		sb.append(result.getCaptureTimestamp());
		sb.append(' ');
		sb.append(result.getOriginalUrl());
		sb.append(' ');
		sb.append(result.getMimeType());
		sb.append(' ');
		sb.append(result.getHttpCode());
		sb.append(' ');
		sb.append(result.getDigest());
		sb.append(" - - ");
		sb.append(result.getCompressedLength());
		sb.append(' ');
		sb.append(result.getOffset());
		sb.append(' ');
		sb.append(result.getFile());
		return sb.toString();
	}

	public CloseableIterator<String> getCDXIterator(String key, String start, String end, ZipNumParams params) throws IOException {
        return loadCDXIterator(key, params.isReverse());
	}
	
	public CloseableIterator<String> getCDXIterator(String key, String prefix, boolean exact, ZipNumParams params) throws IOException {
		return ZipNumIndex.wrapPrefix(loadCDXIterator(key, params.isReverse()), prefix, exact);
	}

	public RedisConnectionManager getRedisMgr() {
		return redisMgr;
	}

	public void setRedisMgr(RedisConnectionManager redisMgr) {
		this.redisMgr = redisMgr;
	}

	@Override
    public String[] lookupPath(String filename) throws IOException {
		
		Jedis jedis = null;
		
		try {
			jedis = redisMgr.getJedisInstance();
			
			String warcPath = jedis.hget(WARC_KEY + filename, WARC_PATH);
			
			if (warcPath != null) {
				return new String[]{warcPath};
			}
			
		} catch (JedisConnectionException jce) {
			redisMgr.returnBrokenJedis(jedis);
			jedis = null;
		} finally {
			redisMgr.returnJedisInstance(jedis);
		}
		
		return EMPTY_STRINGS;
    }

	public int getCdxExpireSec() {
		return cdxExpireSec;
	}

	public void setCdxExpireSec(int cdxExpireSec) {
		this.cdxExpireSec = cdxExpireSec;
	}

	public int getWarcExpireSec() {
		return warcExpireSec;
	}

	public void setWarcExpireSec(int warcExpireSec) {
		this.warcExpireSec = warcExpireSec;
	}

	public int getRemoveOlderThanSec() {
		return removeOlderThanSec;
	}

	public void setRemoveOlderThanSec(int removeOlderThanSec) {
		this.removeOlderThanSec = removeOlderThanSec;
	}

	public boolean isStorePathIndex() {
		return storePathIndex;
	}

	public void setStorePathIndex(boolean storePathIndex) {
		this.storePathIndex = storePathIndex;
	}

	@Override
    public long getTotalLines() {
		long size = 0;
		Jedis jedis = null;
		
		try {
			jedis = redisMgr.getJedisInstance();
			size = jedis.dbSize();
			
		} catch (JedisConnectionException jce) {
			redisMgr.returnBrokenJedis(jedis);
			jedis = null;
		} finally {
			redisMgr.returnJedisInstance(jedis);
		}
		
		return size;
    }

	public String getTotalKey() {
		return totalKey;
	}

	public void setTotalKey(String totalKey) {
		this.totalKey = totalKey;
	}

	public CDXInputSource getTotalInput() {
		return totalInput;
	}

	public void setTotalInput(CDXInputSource totalInput) {
		this.totalInput = totalInput;
	}
}
