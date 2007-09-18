package org.archive.wayback.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPRange {

	// STATIC MEMBERS: 
	private final static Pattern IP_PATTERN = 
		Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)");
	private final static Pattern IP_MASK_PATTERN = 
		Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)/(\\d+)");
	private final static byte[] FULL_MASK = 
		{(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff};
	
	private final static byte[] flags = {
		(byte) 0x80,
		(byte) 0x40,
		(byte) 0x20,
		(byte) 0x10,
		(byte) 0x08,
		(byte) 0x04,
		(byte) 0x02,
		(byte) 0x01,
		};
	
	// INSTANCE MEMBERS: 
	private byte[] ip = null;
	private byte[] mask = null;
	
	// INSTANCE METHODS:
	public byte[] getIp() {
		return ip;
	}

	public byte[] getMask() {
		return mask;
	}
	
	public boolean contains(String ipString) {
		byte[] testIP = matchIP(ipString);
		if(testIP == null) {
			return false;
		}
		return contains(testIP);
	}

	public boolean contains(byte[] testIP) {
		byte[] masked = and(testIP,mask);
		return equals(ip,masked);
	}
	
	public String getRangeString() {
		return null;
	}
	public void setRangeString(String range) {
		setRange(range);
	}
	
	public boolean setRange(String range) {
		Matcher m = IP_MASK_PATTERN.matcher(range);
		if(m != null) {
			if(m.matches()) {
				return setRangeMask(m.group(1),m.group(2));
			}
		}
		return setRangeIP(range);
	}

	// PRIVATE INSTANCE METHODS:
	private boolean setRangeMask(String ipString, String maskBitsString) {
		byte[] tmpMask = maskBits(maskBitsString);
		if(tmpMask != null) {
			if(setRangeIP(ipString)) {
				mask = tmpMask;
				ip = and(ip,mask);
				return true;
			}
		}
		return false;
	}
	private boolean setRangeIP(String ipString) {
		byte[] tmpIp = matchIP(ipString);
		if(tmpIp != null) {
			ip = tmpIp;
			mask = FULL_MASK;
			return true;
		}
		return false;
	}

	// STATIC METHODS:
	public static byte[] maskBits(String bitsString) {
		try {
			int bits = Integer.parseInt(bitsString);
			return maskBits(bits);
		} catch(NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static byte[] maskBits(int bits) {
		byte[] res = new byte[4];
		if(bits < 0) {
			return null;
		}
		if(bits > 32) {
			return null;
		}
		for(int i=0; i < 4; i++) {
			
			int startBit = 8 * i;
			int endBit = 8 * (i+1);
			if(bits < startBit) {
				res[i] = (byte)0x00;
			} else if(bits >= endBit) {
				res[i] = (byte)0xff;
			} else {
				int numOn = bits - startBit;
				int val = 0x00;
				for(int j=0; j < numOn; j++) {
					val |= flags[j];
				}
				res[i] = (byte) val;
			}
		}
		return res;
	}
	public static String bitString(byte b) {
		StringBuilder sb = new StringBuilder(8);
		for(int i=0; i<8; i++) {
			sb.append(((b & flags[i])==0)?"0":"1");
		}
		return sb.toString();
	}
	
	public static byte[] and(byte b1[], byte b2[]) {
		byte[] res = new byte[4];
		for(int i=0; i<4; i++) {
			res[i] = (byte) ((byte) b1[i] & (byte) b2[i]);
		}
		return res;
	}
	public static boolean equals(byte b1[], byte b2[]) {
		for(int i=0; i<4; i++) {
			if(b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}
	public static boolean isOn(byte b, int pos) {
		return (b & flags[pos]) != 0;
	}
	
	public static byte[] matchIP(String ip) {
		Matcher m = IP_PATTERN.matcher(ip);
		if(m != null) {
			if(m.matches()) {
				try {
					byte[] res = new byte[4];
					for(int i=0; i < 4; i++) {
						int testInt = Integer.parseInt(m.group(i+1));
						if(testInt < 0) {
							return null;
						}
						if(testInt > 255) {
							return null;
						}
						res[i] = (byte) testInt;
					}
					return res;
				} catch(NumberFormatException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
	}
}
