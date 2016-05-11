/*
 * Copyright 2015 IIPC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.netpreserve.openwayback.cdxlib.functions;

import org.netpreserve.openwayback.cdxlib.CdxRecord;

/**
 * A function taking two arguments and produce a result.
 */
public interface BiFunction extends Function {

    /**
     * Called by the {@link org.netpreserve.openwayback.cdxlib.processor.BiFunctionProcessor} for
     * every CDX line.
     * <p>
     * For the first invocation, the previous line will be null and the current line will be the
     * first line. For following iterations the previous line will be the line that was current line
     * in the last invocation until this method returns a non null result. When this method returns
     * a non null result, execution will continue with other registered BiFunctions until a final
     * non null result is returned and the iterator will return this as its next value. For the next
     * request for a value from the iterator, the chain of Bifunctions will be evaluated with the
     * previous line set to the last result from the iterator.
     * <p>
     * <h3>Example</h3>
     * A simple deduplication filter could be implemented like this:
     * <pre>
     * public CdxRecord apply(CdxRecord previousLine, CdxRecord currentLine) {
     *     if (previousLine == null || !currentLine.equals(previousLine)) {
     *         return currentLine;
     *     } else {
     *         return null;
     *     }
     * }
     * </pre>
     * <p>
     * @param previousLine the last non null result for previous invocations of this method or null
     * if no such result exists.
     * @param currentLine the current line to be processed or null if there are no more lines.
     * @return the result of the computation. Might be null.
     */
    CdxRecord apply(CdxRecord previousLine, CdxRecord currentLine);

}
