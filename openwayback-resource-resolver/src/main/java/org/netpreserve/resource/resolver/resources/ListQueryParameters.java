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
package org.netpreserve.resource.resolver.resources;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.netpreserve.commons.cdx.SearchKey;
import org.netpreserve.commons.util.datetime.DateTimeRange;
import org.netpreserve.resource.resolver.settings.Settings;

/**
 *
 */
public class ListQueryParameters {

    @PathParam("uri")
    private String uri;

    @QueryParam("recordType")
    @DefaultValue("response,revisit")
    private String recordType;

    @QueryParam("limit")
    @DefaultValue("-1")
    private int limit;

    @QueryParam("date")
    private DateTimeRange dateRange;

    @QueryParam("sort")
    @DefaultValue("ASCENDING")
    private String sort;

    @QueryParam("matchType")
    @DefaultValue("EXACT")
    private SearchKey.UriMatchType uriMatchType;

    @QueryParam("fields")
    @DefaultValue("")
    private String fields;

    @Context
    private Settings settings;

    public String getUri() {
        return uri;
    }

    public SearchKey.UriMatchType getUriMatchType() {
        return uriMatchType;
    }

    public String getRecordType() {
        return recordType;
    }

    public boolean isReverseSort() {
        return sort.toLowerCase().startsWith("desc");
    }

    public int getLimit() {
        return limit > -1 ? limit : settings.getQueryMaxLimit();
    }

    public DateTimeRange getDateRange() {
        return dateRange;
    }

    public String getFieldList() {
        return fields;
    }

}
