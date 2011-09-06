package org.archive.wayback.webapp;

public class PrefixEmbargoPeriodTuple {
	protected String prefix;
	protected long embargoMS;
	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}
	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	/**
	 * @return the embargoMS
	 */
	public long getEmbargoMS() {
		return embargoMS;
	}
	/**
	 * @param embargoMS the embargoMS to set
	 */
	public void setEmbargoMS(long embargoMS) {
		this.embargoMS = embargoMS;
	}
}
