package org.archive.cdxserver.format;

import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

/**
 * CDXFormat serves as customization point for CDX line format.
 * <p>
 * CDX line format had been represented by {@link FieldSplitFormat}, which is essentially
 * a list of field names with methods to manipulate them, and {@link CDXLineFactory}, which
 * serves as a factory of {@link CDXLine} sub-classes.
 * CDXFormat interface encapsulate these two classes behind single interface, and also serves
 * as customization point for the way how certain capture characteristics are represented in
 * CDX line fields, like revisit capture, for example.
 * </p>
 */
public interface CDXFormat {

	/**
	 * Parses {@code input} and return CDXLine.
	 * <p>
	 * This method replaces CDXLineFactory#createStandardCDXLine(String, FieldSplitFormat)
	 * @param input string data from index files.
	 * @return CDXLine
	 */
	public CDXLine createCDXLine(String input);
	
	/**
	 * Return a list of field names.
	 * @return FieldSplitFormat object
	 */
	public FieldSplitFormat getFields();
	
	/**
	 * Return index of field {@code fieldName}.
	 * <p>
	 * Used with {@link CDXLine#getField(int)} to access field data
	 * efficiently.
	 * </p>
	 * @param field
	 * @return zero-based index
	 */
	public int getFieldIndex(String fieldName);
	
	/**
	 * Return new instance with existing fields plus {@code newFields},
	 * maintaining other traits.
	 * <p>
	 * Equivalent of {@link FieldSplitFormat#addFieldNames(String...)}.
	 * </p>
	 * @param newFields
	 * @return CDXFormat
	 */
	public CDXFormat extend(String... newFields);
	
	/**
	 * Implements customizable logic of determining {@code line} is
	 * a revisit capture or not.
	 * @param line capture
	 * @return
	 */
	public boolean isRevisit(CDXLine line);

}
