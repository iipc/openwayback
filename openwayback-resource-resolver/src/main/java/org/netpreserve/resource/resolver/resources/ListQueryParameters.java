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

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import org.netpreserve.resource.resolver.settings.Settings;

/**
 *
 */
public class ListQueryParameters {

    public enum SortType {

        ASCENDING, DESCENDING

    };

    @PathParam("uri")
    private String uri;

    @QueryParam("type")
    @DefaultValue("response,revisit")
    private String type;

    @QueryParam("limit")
    @DefaultValue("-1")
    private int limit;

    @QueryParam("from")
    private String from;

    @QueryParam("to")
    private String to;

    @QueryParam("sort")
    @DefaultValue("ASCENDING")
    private SortType sort;

    @QueryParam("filter")
    private List<String> filter;

    @QueryParam("resolveRevisits")
    private boolean resolveRevisits = false;

    @QueryParam("fields")
    @DefaultValue("")
    private String fields;

    @Context
    private HttpHeaders httpHeaders;

    @Context
    private Settings settings;

    public String getUri() {
        return uri;
    }

    public String getType() {
        return type;
    }

    public SortType getSort() {
        return sort;
    }

    public int getLimit() {
        return limit > -1 ? limit : settings.getQueryMaxLimit();
    }

    public String getAuthToken(String authTokenName) {
        Cookie authCookie = httpHeaders.getCookies().get(authTokenName);
        if (authCookie != null) {
            return authCookie.getValue();
        } else {
            return null;
        }
    }

    public String getFieldList() {
        return fields;
    }

}
