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

import org.netpreserve.openwayback.cdxlib.cdxsource.CdxIterator;
import org.netpreserve.openwayback.cdxlib.functions.FunctionProvider;
import org.netpreserve.openwayback.cdxlib.functions.Function;

import java.util.List;

/**
 * Processing of search results.
 * <p>
 * This could for example be filtering, collapsing, counting or reformatting lines.
 * <p>
 * @param <F> The type of functions this processor handles.
 */
public interface Processor<F extends Function> {

    /**
     * Get the iterator for this processor. The Iterator wraps other processors, which finally wraps
     * a source.
     * <p>
     * @param wrappedIterator the iterator to wrap
     * @return a CdXIterator over the processed result
     */
    CdxIterator processorIterator(CdxIterator wrappedIterator);

    /**
     * Add a function to this processor.
     * <p>
     * The function might be reused in several simultaneously running iterations and should
     * therefore be stateless and thread safe. If keeping state is needed
     * {@link #addFunctionProvider(org.netpreserve.openwayback.cdxlib.functions.FunctionProvider)}
     * should be used instead.
     * <p>
     * @param function the function to add
     * @return returns this processor for easy chaining of calls
     */
    Processor<F> addFunction(F function);

    /**
     *
     * @param functionProvider
     * @return
     */
    Processor<F> addFunctionProvider(FunctionProvider<F> functionProvider);

    /**
     * Gets a list of functions ready for use in one iteration.
     * <p>
     * Functions are returned unaltered and functionproviders gets their
     * {@link FunctionProvider#newCdxFunction()} called to create a new instance.
     * <p>
     * @return the list of functions.
     */
    List<F> getInstanciatedFunctions();

}
