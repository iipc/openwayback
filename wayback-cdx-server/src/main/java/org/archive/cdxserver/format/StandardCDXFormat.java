package org.archive.cdxserver.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.archive.format.cdx.CDXFieldConstants;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.CDXLineFactory;
import org.archive.format.cdx.FieldSplitFormat;
import org.archive.format.cdx.StandardCDXLineFactory;

/**
 * Implementation of {@link CDXFormat} for traditional CDX9 and CDX11 format.
 * <p>
 * Serves as a bridge to traditional combination of {@link FieldSplitFormat} and
 * {@link StandardCDXLineFactory}.
 * You can define extended CDXFormat by providing custom {@link CDXLineFactory} implementation.
 * StandardCDXLineFactory can only support CDX11 and CDX9 in space-delimited lines efficiently.
 * </p>
 * <p>
 * This class has no convenience constructor taking symbolic format name (like {@code "cdx11"}) as
 * the first argument, because it can be confusing.
 * </p>
 * <p>
 * Note: if you ever need to make a sub-class of this class for modified traits, be sure
 * to override {@link #copy()}, so that it {@link #extend(String...)} creates new instance of
 * appropriate type.
 * </p>
 */
public class StandardCDXFormat extends FieldSplitFormat implements CDXFormat, CDXFieldConstants {
	// CDX11Line and CDX9Line have package-scope constructor. We need to use CDXLineFactory
	// to create their instance.
	private final CDXLineFactory factory;
	
	public StandardCDXFormat(CDXLineFactory factory, String... names) {
		super(Arrays.asList(names));
		this.factory = factory;
	}
	
	protected StandardCDXFormat(CDXLineFactory factory, List<String> names) {
		super(names);
		this.factory = factory;
	}

	/**
	 * Create new instance with the same internal data.
	 * @return
	 */
	protected StandardCDXFormat copy() {
		// need to make a copy because it will be stored as it is in
		// FieldSplitFormat.
		List<String> namesCopy = new ArrayList<String>(names);
		return new StandardCDXFormat(factory, namesCopy);
	}
	
	/**
	 * Destructively add {@code newFields} to {@code names}.
	 * @param newFields
	 */
	protected void addFields(List<String> newFields) {
		// note we cannot use FieldSplitFormat.addFieldNames(String...) because it
		// creates a new instance of FieldSplitFormat (i.e. not destructive).
		int i = names.size();
		for (String name : newFields) {
			nameToIndex.put(name, i++);
		}
		names.addAll(newFields);
	}
	
	public CDXLine createCDXLine(String input) {
		return factory.createStandardCDXLine(input, this);
	}
	
	public boolean isRevisit(CDXLine line) {
		// standard revisit representation: mimetype is "warc/revisit" (WARC) or
		// filename is empty ("-") (ARC)
		return line.getMimeType().equals("warc/revisit") ||
				line.getFilename().equals(CDXLine.EMPTY_VALUE);
	}
	
	@Override
	public FieldSplitFormat getFields() {
		return this;
	}

	@Override
	public final CDXFormat extend(String... moreFields) {
		if (moreFields.length == 0)
			return this;
		StandardCDXFormat extended = copy();
		extended.addFields(Arrays.asList(moreFields));
		return extended;
	}
}
