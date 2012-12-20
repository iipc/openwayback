package org.archive.wayback.resourceindex.ziplines;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;

import org.archive.util.zip.OpenJDK7GZIPInputStream;

public class ZiplinedMultiBlock extends ZiplinedBlock {
	
	//long offset = 0;
	String partName;
	//String[] locations;
	BlockLoader loader;
	ArrayList<Integer> blockSizes;
	//int totalSize = 0;
	
	public ZiplinedMultiBlock(long offset, BlockLoader loader, String partName, String[] locations)
	{
		super(locations, offset, 0);
		setLoader(loader);
		this.partName = partName;
		blockSizes = new ArrayList<Integer>();
	}
	
	public boolean isSameBlock(long nextOffset, String nextPartName)
	{
		return ((offset + super.count) == nextOffset) && partName.equals(nextPartName);
	}
	
	public void addOffset(int size)
	{
		blockSizes.add(size);
		super.count += size;
	}
	
	@Override
	public String toString()
	{
		return "Multiblock from " + partName + " of " + blockSizes.size() + " segments (" + offset + "," + super.count + ")";
	}
	
	@Override
	public BufferedReader readBlock()
	throws IOException {
		byte bytes[] = attemptBlockLoad(super.loader);
		
		if(bytes == null) {
			throw new IOException("Unable to load block(s)!");
		}
		
		int count = 0;
		InputStream currChain = null;
		
		for (int blockSize : blockSizes) {
			InputStream nextStream = new ByteArrayInputStream(bytes, count, blockSize);
			nextStream = new OpenJDK7GZIPInputStream(nextStream);
			
			if (currChain == null) {
				currChain = nextStream;
			} else {
				currChain = new SequenceInputStream(currChain, nextStream);
			}
			
			count += blockSize;
		}
		
		return new BufferedReader(new InputStreamReader(currChain));
	}
}
