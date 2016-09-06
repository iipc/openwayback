/*
 * Copyright 2016 IIPC.
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
package org.netpreserve.resource.resolver.resources;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.netpreserve.resource.resolver.settings.Settings;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.SearchKey;
import org.netpreserve.commons.cdx.SearchResult;
import org.netpreserve.commons.cdx.cdxsource.ClosestSearchResult;
import org.netpreserve.commons.cdx.functions.Filter;
import org.netpreserve.commons.cdx.functions.RecordTypeFilter;
import org.netpreserve.commons.cdx.processor.FilterProcessor;
import org.netpreserve.commons.cdx.processor.Processor;
import org.netpreserve.commons.util.datetime.VariablePrecisionDateTime;

/**
 *
 */
@Path("resource")
@Singleton
public class LookupResource {

    @Context
    Settings settings;

    @Context
    CdxSource cdxSource;

    @GET
    @Path("{uri}/{timestamp}")
    @Produces({"application/vnd.org.netpreserve.cdxj",
        "application/vnd.org.netpreserve.cdx",
        MediaType.TEXT_PLAIN + "; charset=\"UTF-8\"",
        MediaType.TEXT_HTML})
    public SearchResult getCdx(@BeanParam LookupQueryParameters params) {
        SearchKey key = new SearchKey().uri(params.getUri(), SearchKey.UriMatchType.EXACT);
        VariablePrecisionDateTime time = VariablePrecisionDateTime.valueOf(params.getTimestamp());

        List<Processor> processors = new ArrayList<>();

        if (params.getRecordType() != null) {
            RecordTypeFilter rtf = new RecordTypeFilter(params.getRecordType().split("\\s*,\\s*"));
            Processor<Filter> fp = new FilterProcessor().addFunction(rtf);
            processors.add(fp);
        }

        return new ClosestSearchResult(cdxSource, key, time, processors).limit(params.getLimit());
    }

}
