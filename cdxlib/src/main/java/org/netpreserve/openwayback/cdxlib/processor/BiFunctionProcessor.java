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
import org.netpreserve.openwayback.cdxlib.CdxRecord;
import org.netpreserve.openwayback.cdxlib.functions.BiFunction;

/**
 * Processor taking a set of {@link BiFunction}'s and returning the evaluation of those.
 * <p>
 * @param <T> The type of {@link BiFunction} supported by this processor.
 */
public class BiFunctionProcessor<T extends BiFunction> extends AbstractProcessor<T> {

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
            protected CdxRecord computeNext() {
                CdxRecord result = null;
                while (wrappedCdxIterator.hasNext()) {
                    result = applyFunction(0, wrappedCdxIterator.next());

                    if (result != null) {
                        return result;
                    }
                }
                return endOfData();
            }

            private CdxRecord applyFunction(int functionIdx, CdxRecord currentLine) {
                CdxRecord result = functions.get(functionIdx).apply(currentLine);
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

        private CdxRecord previousLine = null;

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
        CdxRecord apply(CdxRecord currentLine) {
            CdxRecord result = function.apply(previousLine, currentLine);

            previousLine = currentLine;
            return result;
        }

    }
}
