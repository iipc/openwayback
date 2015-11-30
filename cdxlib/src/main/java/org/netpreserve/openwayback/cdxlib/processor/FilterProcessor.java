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

import org.netpreserve.openwayback.cdxlib.functions.Filter;
import org.netpreserve.openwayback.cdxlib.cdxsource.CdxIterator;
import org.netpreserve.openwayback.cdxlib.CdxLine;

/**
 * Processor taking a set of {@link Filter}'s and returning only those CDX lines mathing all of the
 * filters.
 */
public class FilterProcessor extends AbstractProcessor<Filter> {

    @Override
    public CdxIterator processorIterator(CdxIterator wrappedIterator) {
        return new AbstractProcessorIterator<Filter>(wrappedIterator) {
            private final List<Filter> filters = getInstanciatedFunctions();

            @Override
            protected CdxLine computeNext() {
                if (wrappedCdxIterator.hasNext()) {
                    CdxLine input = wrappedCdxIterator.next();
                    boolean include = true;
                    for (Filter filter : filters) {
                        if (filter != null && !filter.include(input)) {
                            include = false;
                            break;
                        }
                    }
                    if (include) {
                        return input;
                    }
                    return null;
                } else {
                    return endOfData();
                }
            }

        };
    }

}
