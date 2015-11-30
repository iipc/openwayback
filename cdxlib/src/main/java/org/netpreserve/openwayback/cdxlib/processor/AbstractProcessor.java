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

import org.netpreserve.openwayback.cdxlib.functions.Filter;
import org.netpreserve.openwayback.cdxlib.functions.Function;
import org.netpreserve.openwayback.cdxlib.functions.FunctionProvider;

/**
 *
 */
public abstract class AbstractProcessor<F extends Function> implements Processor<F> {

    List<Object> functions;

    public AbstractProcessor() {
        this.functions = new ArrayList<>();
    }

    @Override
    public Processor<F> addFunction(F function) {
        if (function != null) {
            functions.add(function);
        }
        return this;
    }

    @Override
    public Processor<F> addFunctionProvider(FunctionProvider<F> functionProvider) {
        if (functionProvider != null) {
            functions.add(functionProvider);
        }
        return this;
    }

    @Override
    public List<F> getInstanciatedFunctions() {
        List<F> instanciatedFunctions = new ArrayList<>(functions.size());
        for (Object f : functions) {
            if (f instanceof Function) {
                instanciatedFunctions.add((F) f);
            } else {
                instanciatedFunctions.add(((FunctionProvider<F>) f).newCdxFunction());
            }
        }
        return instanciatedFunctions;
    }

}
