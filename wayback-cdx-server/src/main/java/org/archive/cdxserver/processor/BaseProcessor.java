package org.archive.cdxserver.processor;

import org.archive.cdxserver.writer.CDXWriter;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;

/**
 * {@code BaseProcessor} is an interface for a receiver
 * of {@link CDXLine}s.
 * <p>Implementation may be final output formatter ({@link CDXWriter}
 * subclasses for example), or an intermediary processor that performs
 * transformation and/or filtering on the sequence of CDXLines.</p>
 * <p>{@code CDXServer} starts from {@code CDXWriter} and builds up
 * nested pipeline of {@code BaseProcessor}s, and then calls following
 * methods in sequence on the {@code BaseProcessor} at the top:
 * <ol>
 * <li>modifyOutputFormat(FieldSplitFormat)</li>
 * <li>begin()</li>
 * <li>for each CDXLine:</li>
 * <ol>
 * <li>trackLine(CDXLine)</li>
 * <li>writeLine(CDXLine)</li>
 * </ol>
 * <li>writeResumeKey(String) (if {@code showResumeKey})</li>
 * <li>end()</li>
 * </ol>
 */
public interface BaseProcessor {
	/**
	 * This method will be called just before looping over
	 * the sequence of CDX lines.
	 * Intermediary processor must call {@code begin()}
	 * on nested processor.
	 */
	public void begin();
	
	/**
	 * Called on each CDX line, just before timestamp range filtering
	 * (to and from parameters), regexp filtering, and {@code collapser}
	 * processing. Typically used for counting the number of CDX lines
	 * collapsed / grouped.
	 * @param line CDX line
	 */
	public void trackLine(CDXLine line);

	/**
	 * Process {@code line}.
	 * @param line {@code CDXLine}
	 * @return 1 if {@code line} is sent to output, 0 otherwise.
	 */
	public int writeLine(CDXLine line);

	/**
	 * Write resumption key.
	 * Only the final {@code CDXWriter} should do
	 * actual work. All intermediaries shall simply call
	 * {@code writeResumeKey(resumeKey)} on nested processor.
	 * @param resumeKey
	 */
	public void writeResumeKey(String resumeKey);

	/**
	 * Called at the end.
	 * Perform any clean ups / finalizations here.
	 * Intermediaries processor should call {@code end()}
	 * on nested processor.
	 */
	public void end();

	/**
	 * Return output format (list of fields), given input format {@code format}.
	 * Intermediaries should call {@code modifyOutputFormat(format)} on nested
	 * processor first, then make appropriate changes to it if they add/remove
	 * fields.
	 * @param format input format
	 * @return output format
	 * @see CDXFieldConstants
	 */
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format);
}
