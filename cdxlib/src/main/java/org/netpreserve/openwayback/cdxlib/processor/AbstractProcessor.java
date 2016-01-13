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

import org.netpreserve.openwayback.cdxlib.functions.Function;
import org.netpreserve.openwayback.cdxlib.functions.FunctionProvider;

/**
 * Base implementation of {@link Processor}.
 * <p>
 * @param <T> The type of {@link Function} supported by this processor.
 */
public abstract class AbstractProcessor<T extends Function> implements Processor<T> {

    private final List<Object> functions;

    /**
     * Default constructor.
     */
    public AbstractProcessor() {
        this.functions = new ArrayList<>();
    }

    @Override
    public Processor<T> addFunction(T function) {
        if (function != null) {
            functions.add(function);
        }
        return this;
    }

    @Override
    public Processor<T> addFunctionProvider(FunctionProvider<T> functionProvider) {
        if (functionProvider != null) {
            functions.add(functionProvider);
        }
        return this;
    }

    @Override
    public List<T> getInstanciatedFunctions() {
        List<T> instanciatedFunctions = new ArrayList<>(functions.size());
        for (Object f : functions) {
            if (f instanceof Function) {
                instanciatedFunctions.add((T) f);
            } else {
                instanciatedFunctions.add(((FunctionProvider<T>) f).newFunction());
            }
        }
        return instanciatedFunctions;
    }

}
