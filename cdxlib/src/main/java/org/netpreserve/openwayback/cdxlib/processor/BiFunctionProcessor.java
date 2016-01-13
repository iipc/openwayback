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
package org.netpreserve.openwayback.cdxlib.processor;

import java.util.ArrayList;
import java.util.List;

import org.netpreserve.openwayback.cdxlib.cdxsource.CdxIterator;
import org.netpreserve.openwayback.cdxlib.CdxLine;
import org.netpreserve.openwayback.cdxlib.functions.BiFunction;

/**
 * Processor taking a set of {@link BiFunction}'s and returning the evaluation of those.
 * <p>
 * @param <T> The type of {@link BiFunction} supported by this processor.
 */
public class BiFunctionProcessor<T extends BiFunction> extends AbstractProcessor<T> {
    public static final String COLLAPSE_COUNT_FIELD_NAME = "collapseCount";

    @Override
    public CdxIterator processorIterator(CdxIterator wrappedIterator) {
        List<T> submittedFunctions = getInstanciatedFunctions();
        if (submittedFunctions.isEmpty()) {
            return wrappedIterator;
        }

        final List<BiFunctionWrapper> functions = new ArrayList<>(submittedFunctions.size());
        for (T sf : submittedFunctions) {
            functions.add(new BiFunctionWrapper(sf));
        }

        return new AbstractProcessorIterator<BiFunction>(wrappedIterator) {
            @Override
            protected CdxLine computeNext() {
                CdxLine result = null;
                while (wrappedCdxIterator.hasNext()) {
                    result = applyFunction(0, wrappedCdxIterator.next());

                    if (result != null) {
                        return result;
                    }
                }
                return endOfData();
            }

            private CdxLine applyFunction(int functionIdx, CdxLine currentLine) {
                CdxLine result = functions.get(functionIdx).apply(currentLine);
                if (result != null && functionIdx + 1 < functions.size()) {
                    return applyFunction(functionIdx + 1, result);
                } else {
                    return result;
                }
            }

        };
    }

    /**
     * Helper class to keep necessary state for each function during an iteration.
     */
    private static class BiFunctionWrapper {

        private final BiFunction function;

        private CdxLine previousLine = null;

        /**
         * Construct a wrapper taking the function to wrap as the sole argument.
         * <p>
         * @param function the function to wrap
         */
        public BiFunctionWrapper(BiFunction function) {
            this.function = function;
        }

        /**
         * Process the current line with the wrapped function.
         * <p>
         * @param currentLine the line to process
         * @return the result of processing the line. Might be {@code null} indicating that the
         * current line should be skipped or collapsed
         */
        CdxLine apply(CdxLine currentLine) {
            CdxLine result = function.apply(previousLine, currentLine);

            if (currentLine.hasMutableField(COLLAPSE_COUNT_FIELD_NAME)) {
                updateCounter(currentLine, result);
            }

            previousLine = currentLine;
            return result;
        }

        /**
         * Update a counter telling how man lines was collapsed into the current one.
         * <p>
         * A line is considered collapsed when the result parameter is {@code null}
         * <p>
         * @param currentLine the current CDX line
         * @param result the result of the functions processing the current line.
         */
        private void updateCounter(CdxLine currentLine, CdxLine result) {
            if (previousLine == null) {
                if (currentLine.getMutableField(COLLAPSE_COUNT_FIELD_NAME) == null) {
                    currentLine.setMutableField(COLLAPSE_COUNT_FIELD_NAME, 1);
                }
            } else {
                int prevCount;
                if (result == null) {
                    prevCount = (int) previousLine.getMutableField(COLLAPSE_COUNT_FIELD_NAME);
                } else {
                    prevCount = 0;
                }

                int curCount = 1;
                if (currentLine.getMutableField(COLLAPSE_COUNT_FIELD_NAME) != null) {
                    curCount = (int) currentLine.getMutableField(COLLAPSE_COUNT_FIELD_NAME);
                }
                currentLine.setMutableField(COLLAPSE_COUNT_FIELD_NAME, prevCount + curCount);
            }
        }

    }
}
