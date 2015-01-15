package org.archive.wayback.webapp;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PerfStats
{	
	private static final Logger LOGGER = Logger.getLogger(
			PerfStats.class.getName());
	/**
	 * output format constants.
	 */
	public enum OutputFormat {
		BRACKET,
		JSON
	};

	public static class PerfStatEntry
	{
		String name;
		long start;
		long total;
		int count;
		boolean isErr;
		
		public PerfStatEntry(String name)
		{
			this.name = name;
			clear();
		}
		
		public void clear()
		{
			start = 0;
			total = 0;
			count = 0;
			isErr = false;
		}
		
		public void startNow()
		{
			if (start > 0) {
				isErr = true;
			}
			start = System.currentTimeMillis();
		}
		
		public long endNow()
		{
			long elapsed = 0;
			
			if (start > 0) {
				long end = System.currentTimeMillis();
				elapsed = (end - start);
				count++;
			} else {
				isErr = true;
			}
			start = 0;
			total += elapsed;
			return elapsed;
		}
		
		public String toString()
		{
			// Skip perf stats that haven't been set at all
			if (count == 0 && total == 0) {
				return "";
			}
			
			if (start > 0) {
				isErr = true;
			}
			StringBuilder builder = new StringBuilder(name);
			builder.append(": ");
			builder.append(total);
//			builder.append(" ");
//			builder.append(count);
			if (isErr) {
				builder.append(" ERR");
			}
			return builder.toString();
		}
		
		public long getTotal()
		{
			return total;
		}
	}
	
	static ThreadLocal<Map<String, PerfStatEntry>> perfStats = new ThreadLocal<Map<String, PerfStatEntry>>()
	{
		@Override
		protected Map<String, PerfStatEntry> initialValue() {
			return new TreeMap<String, PerfStatEntry>();
		}
	};
	
	static ThreadLocal<PerfStatEntry> lastEntry = new ThreadLocal<PerfStatEntry>()
	{
		@Override
		protected PerfStatEntry initialValue() {
			return null;
		}		
	};
	
	public static PerfStatEntry get(String statName)
	{
		PerfStatEntry entry = lastEntry.get();
		
		if ((entry != null) && entry.name.equals(statName)) {
			return entry;
		}
			
		entry = perfStats.get().get(statName);
		
		if (entry == null) {
			entry = new PerfStatEntry(statName);
			perfStats.get().put(statName, entry);
		}
		
		lastEntry.set(entry);
		return entry;
	}

	public static long getTotal(Enum<?> stat) {
		return getTotal(stat.toString());
	}

	public static long getTotal(String statName) {
		PerfStatEntry entry = get(statName);
		return entry != null ? entry.getTotal() : 0;
	}
	
	public static void clearAll()
	{
		lastEntry.set(null);
		
		for (PerfStatEntry entry : perfStats.get().values()) {
			entry.clear();
		}
	}
	
	public static void timeStart(Enum<?> stat)
	{
		timeStart(stat.toString());
	}
	
	public static void timeStart(String statName)
	{
		get(statName).startNow();
	}
	
	public static long timeEnd(Enum<?> stat)
	{
		return timeEnd(stat.toString(), true);
	}
	
	public static long timeEnd(Enum<?> stat, boolean log)
	{
		return timeEnd(stat.toString(), log);
	}
	
	public static long timeEnd(String statName)
	{
		return timeEnd(statName, true);
	}
	
	public static long timeEnd(String statName, boolean log)
	{
		long elapsed = get(statName).endNow();
		
		if (log && LOGGER.isLoggable(Level.INFO)) {
			StringBuilder builder = new StringBuilder();		
			builder.append("WB-PERF\t");
			builder.append(statName);
			builder.append("\t");
			builder.append(elapsed);
			LOGGER.info(builder.toString());
		}
		
		return elapsed;
	}
	
	public static String getAllStats() {
		return getAllStats(OutputFormat.BRACKET);
	}

	/**
	 * Format all stats in the format specified.
	 * @param format serialization format
	 * @return serialized stats
	 */
	public static String getAllStats(OutputFormat format) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		
		Collection<PerfStatEntry> stats = perfStats.get().values();
		
		switch (format) {
		case JSON:
			sb.append('{');
			for (PerfStatEntry entry : stats) {
				if (entry.count > 0) {
					if (first) {
						first = false;
					} else {
						sb.append(',');
					}
					sb.append('"').append(entry.name).append("\":");
					if (entry.isErr)
						sb.append("null");
					else
						sb.append(entry.total);
				}
			}
			sb.append('}');
			break;
		default:
			sb.append("[");
			for (PerfStatEntry entry : stats) {
				if (entry.count > 0) {
					if (first) {
						first = false;
					} else {
						sb.append(", ");
					}
					sb.append(entry.toString());
				}
			}			
			sb.append("]");
			break;
		}
		return sb.toString();
	}
}
