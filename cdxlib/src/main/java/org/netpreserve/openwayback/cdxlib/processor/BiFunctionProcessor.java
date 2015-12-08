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
        final List<F> functions = getInstanciatedFunctions();
        if (functions.isEmpty()) {
            return wrappedIterator;
        }

        return new AbstractProcessorIterator<BiFunction>(wrappedIterator) {
            private CdxLine previousLine = null;

            @Override
            protected CdxLine computeNext() {
                if (wrappedCdxIterator.hasNext()) {
                    CdxLine result = null;
                    for (BiFunction function : functions) {
                        result = computeNextForOneBiFunction(function);
                    }
                    return result;
                } else {
                    return endOfData();
                }
            }

            private CdxLine computeNextForOneBiFunction(BiFunction function) {
                CdxLine result = null;
                while (result == null && wrappedCdxIterator.hasNext()) {
                    CdxLine input = wrappedCdxIterator.next();
                    result = function.apply(previousLine, input);
                    previousLine = input;
                }
                if (result != null) {
                    return result;
                } else {
                    return endOfData();
                }
            }

        };
    }

}
