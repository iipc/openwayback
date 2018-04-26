package org.archive.cdxserver.processor;

import org.archive.cdxserver.format.CDXFormat;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

/**
 * Processor that selects specified fields in specified order.
 * <p>
 * This processor is inserted just before the final CDX writer,
 * and creates new CDXLine with only those fields that goes out
 * in the output, if output fields are different from original
 * index format.
 * </p>
 * <p>
 * Note {@link CDXLine} passed downstream will have raw {@link FieldSplitFormat}
 * object as {@code names} if this processor actually takes effect. Therefore,
 * no further processing shall be done except for writing out to the final output.
 * </p>
 * @author kenji
 *
 */
public class FieldSelectorProcessor extends WrappedProcessor {
	private FieldSplitFormat allowedFields;
	
	private FieldSplitFormat outputFields;
	
	/**
	 * Initialize with {@code outputFields} and {@code allowedFields}.
	 * <p>
	 * Note if both {@code outputFields} and {@code allowedFields} are {@code null},
	 * There's no point using this processor.
	 * </p>
	 * @param downstream downstream processor (final writer)
	 * @param outputFields fields requested by client, may be {@code null}
	 * @param allowedFields fields allowed for client privilege level, may be {@code null}
	 */
	public FieldSelectorProcessor(BaseProcessor downstream,
			FieldSplitFormat outputFields, FieldSplitFormat allowedFields) {
		super(downstream);
		if (allowedFields != null && outputFields != null) {
			outputFields = allowedFields.createSubset(outputFields);
		}
		this.allowedFields = allowedFields;
		this.outputFields = outputFields;
	}
	
	@Override
	public int writeLine(CDXLine line) {
		if (outputFields != null) {
			line = new CDXLine(line, outputFields);
		}
		return super.writeLine(line);
	}
	
	@Override
	public CDXFormat modifyOutputFormat(CDXFormat format) {
		// assumes format has all fields added by upstream. for this
		// reason, upstream processor must call downstream modifyOutputFormat()
		// after adding fields they use.
		if (outputFields != null) {
			// allowedFields is already applied if non-null
			outputFields = format.getFields().createSubset(outputFields);
		} else if (allowedFields != null) {
			outputFields = allowedFields.createSubset(format.getFields());
		} else {
			// if outputFields is null and allowedFields is null, no need for
			// selecting. outputFields can remain null.
		}
		return super.modifyOutputFormat(format);
	}
}
