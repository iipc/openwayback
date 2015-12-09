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
 */
public class BiFunctionProcessor<F extends BiFunction> extends AbstractProcessor<F> {

    @Override
    public CdxIterator processorIterator(CdxIterator wrappedIterator) {
        List<F> submittedFunctions = getInstanciatedFunctions();
        if (submittedFunctions.isEmpty()) {
            return wrappedIterator;
        }

        final List<BiFunctionWrapper> functions = new ArrayList<>(submittedFunctions.size());
        for (F sf : submittedFunctions) {
            functions.add(new BiFunctionWrapper(sf));
        }

        return new AbstractProcessorIterator<BiFunction>(wrappedIterator) {
            @Override
            protected CdxLine computeNext() {
                CdxLine result = null;
                int functionIdx = 0;
                BiFunctionWrapper function = functions.get(functionIdx);
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

    private static class BiFunctionWrapper {

        private final BiFunction function;

        private CdxLine previousLine = null;

        public BiFunctionWrapper(BiFunction function) {
            this.function = function;
        }

        CdxLine apply(CdxLine currentLine) {
            CdxLine result = function.apply(previousLine, currentLine);
            previousLine = currentLine;
            return result;
        }

    }
}
