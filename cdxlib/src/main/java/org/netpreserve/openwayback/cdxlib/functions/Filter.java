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
 * Function for deciding if a CDX line should be included in the result.
 */
public interface Filter extends Function {

    /**
     * Called by the {@link org.netpreserve.openwayback.cdxlib.processor.FilterProcessor} for every
     * CDX line.
     * <p>
     * @param line the line to evaluate.
     * @return true if line should be included in the result.
     */
    public boolean include(CdxRecord line);

}
