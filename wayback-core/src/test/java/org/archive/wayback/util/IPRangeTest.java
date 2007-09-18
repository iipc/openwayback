package org.archive.wayback.util;

import junit.framework.TestCase;

public class IPRangeTest extends TestCase {
	public void testBitOn() {
		byte b = (byte) 0xFF;
		for(int i=0; i<8; i++) {
			assertTrue(IPRange.isOn(b, i));
		}
		b = (byte) 0x80;
		assertTrue(IPRange.isOn(b, 0));
		for(int i=1; i<8; i++) {
			assertFalse(IPRange.isOn(b, i));
		}
		b = (byte) 0xc0;
		assertTrue(IPRange.isOn(b, 0));
		assertTrue(IPRange.isOn(b, 1));
		for(int i=2; i<8; i++) {
			assertFalse(IPRange.isOn(b, i));
		}
		b |= 0x01;
		assertTrue(IPRange.isOn(b, 0));
		assertTrue(IPRange.isOn(b, 1));
		assertTrue(IPRange.isOn(b, 7));
		for(int i=2; i<7; i++) {
			assertFalse(IPRange.isOn(b, i));
		}
		
		b = (byte) 0xf0 & (byte) 0x00;
		for(int i=0; i<8; i++) {
			assertFalse(IPRange.isOn(b, i));
		}

		b = (byte) 0xf0 & (byte) 0x80;
		assertTrue(IPRange.isOn(b, 0));
		for(int i=1; i<8; i++) {
			assertFalse(IPRange.isOn(b, i));
		}
	}
	public void testBitString() {
		assertEquals("00000000",IPRange.bitString((byte)0x00));
		assertEquals("11111111",IPRange.bitString((byte)0xff));
		assertEquals("11110000",IPRange.bitString((byte)0xf0));
		assertEquals("11100000",IPRange.bitString((byte)0xe0));
		assertEquals("11000000",IPRange.bitString((byte)0xc0));
		assertEquals("11001100",IPRange.bitString((byte)0xcc));
		assertEquals("11001101",IPRange.bitString((byte)0xcd));
		assertEquals("00010000",IPRange.bitString((byte)0x10));
		assertEquals("00010001",IPRange.bitString((byte)0x11));
	}
	
	public void testMask() {
		byte[] b = IPRange.maskBits(0);
		assertEquals("00000000",IPRange.bitString(b[0]));
		assertEquals("00000000",IPRange.bitString(b[1]));
		assertEquals("00000000",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));

		b = IPRange.maskBits(1);
		assertEquals("10000000",IPRange.bitString(b[0]));
		assertEquals("00000000",IPRange.bitString(b[1]));
		assertEquals("00000000",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));

		b = IPRange.maskBits(2);
		assertEquals("11000000",IPRange.bitString(b[0]));
		assertEquals("00000000",IPRange.bitString(b[1]));
		assertEquals("00000000",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));

		b = IPRange.maskBits(9);
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("10000000",IPRange.bitString(b[1]));
		assertEquals("00000000",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));

		b = IPRange.maskBits(23);
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111110",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));

		b = IPRange.maskBits(30);
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111111",IPRange.bitString(b[2]));
		assertEquals("11111100",IPRange.bitString(b[3]));

		b = IPRange.maskBits(31);
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111111",IPRange.bitString(b[2]));
		assertEquals("11111110",IPRange.bitString(b[3]));

		b = IPRange.maskBits(32);
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111111",IPRange.bitString(b[2]));
		assertEquals("11111111",IPRange.bitString(b[3]));

	}
	public void testParse() {
		IPRange r = new IPRange();

		assertFalse(r.setRange("127.0.0."));
		assertFalse(r.setRange("256.0.0.1"));
		assertFalse(r.setRange("0.256.0.0.1"));
		assertFalse(r.setRange("0.256.0.0"));
		assertFalse(r.setRange("0.0.0.256"));
		
		assertTrue(r.setRange("127.0.0.1"));
		assertTrue(r.setRange("255.0.0.0"));
		assertTrue(r.setRange("0.0.0.255"));
		assertTrue(r.setRange("127.127.127.127"));
		assertTrue(r.setRange("127.127.127.255"));
		
		assertTrue(r.setRange("128.0.0.0"));
		byte[] b = r.getMask();
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111111",IPRange.bitString(b[2]));
		assertEquals("11111111",IPRange.bitString(b[3]));
		b = r.getIp();
		assertEquals("10000000",IPRange.bitString(b[0]));
		assertEquals("00000000",IPRange.bitString(b[1]));
		assertEquals("00000000",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));
		
		assertTrue(r.setRange("129.0.0.0"));
		b = r.getMask();
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111111",IPRange.bitString(b[2]));
		assertEquals("11111111",IPRange.bitString(b[3]));
		b = r.getIp();
		assertEquals("10000001",IPRange.bitString(b[0]));
		assertEquals("00000000",IPRange.bitString(b[1]));
		assertEquals("00000000",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));
		
		assertTrue(r.setRange("129.0.0.0/30"));
		b = r.getMask();
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111111",IPRange.bitString(b[2]));
		assertEquals("11111100",IPRange.bitString(b[3]));
		b = r.getIp();
		assertEquals("10000001",IPRange.bitString(b[0]));
		assertEquals("00000000",IPRange.bitString(b[1]));
		assertEquals("00000000",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));
		
		assertTrue(r.setRange("129.129.129.129/24"));
		b = r.getMask();
		assertEquals("11111111",IPRange.bitString(b[0]));
		assertEquals("11111111",IPRange.bitString(b[1]));
		assertEquals("11111111",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));
		b = r.getIp();
		assertEquals("10000001",IPRange.bitString(b[0]));
		assertEquals("10000001",IPRange.bitString(b[1]));
		assertEquals("10000001",IPRange.bitString(b[2]));
		assertEquals("00000000",IPRange.bitString(b[3]));
	}
	
	public void testContains() {
		IPRange r = new IPRange();
		assertTrue(r.setRange("129.129.129.0/24"));
		assertTrue(r.contains("129.129.129.129"));
		assertTrue(r.contains("129.129.129.255"));
		assertTrue(r.contains("129.129.129.0"));
		assertFalse(r.contains("129.129.128.0"));
		assertFalse(r.contains("129.129.128.255"));

		assertTrue(r.setRange("129.129.129.129/24"));
		assertTrue(r.contains("129.129.129.129"));
		assertTrue(r.contains("129.129.129.255"));
		assertTrue(r.contains("129.129.129.0"));
		assertFalse(r.contains("129.129.128.0"));
		assertFalse(r.contains("129.129.128.255"));
		
		assertTrue(r.setRange("129.129.129.129/25"));
		assertTrue(r.contains("129.129.129.128"));
		assertTrue(r.contains("129.129.129.129"));
		assertTrue(r.contains("129.129.129.255"));
		assertFalse(r.contains("129.129.129.0"));
		assertFalse(r.contains("129.129.128.0"));
		assertFalse(r.contains("129.129.128.255"));
	}
}
