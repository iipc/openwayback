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
package org.netpreserve.openwayback.cdxlib;

import org.netpreserve.openwayback.cdxlib.processor.Processor;

import java.io.Closeable;
import java.util.List;

/**
 * Representation of a source of Cdx lines.
 * <p>
 * Could be a file, a compressed Cdx cluster or some other source like a database or a web service.
 */
public interface CdxSource extends Closeable {

    /**
     * Get a list of Cdx lines.
     *
     * @param startUrl the lexiografically lowest url (inclusive) to get
     * @param endUrl the lexiografically highest url (exclusive) to get
     * @param outputFormat the format the line should be converted to
     * @param processors a list of processors for filtering the list
     * @param reverse if true the result list will be sorted in descending order
     * @return an {@link SearchResult} returning iterators over the requested list
     */
    SearchResult search(String startUrl, String endUrl, CdxLineSchema outputFormat,
            List<Processor> processors, boolean reverse);

}
