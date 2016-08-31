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
package org.netpreserve.resource.resolver.jaxrs;

import java.util.regex.Pattern;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.netpreserve.resource.resolver.settings.Settings;

/**
 * Filter adding response headers for allowing cross origin ajax requests.
 */
@Provider
public class CrossOriginResourceSharingFilter implements ContainerResponseFilter {

    private final Pattern corsAllowedOriginPattern;

    /**
     * Constructor with injected Settings.
     * <p>
     * @param settings Settings for configuring the pattern used for cross site access control.
     */
    public CrossOriginResourceSharingFilter(@Context final Settings settings) {
        corsAllowedOriginPattern = Pattern.compile(settings.getCorsAllowedOriginPattern());
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        String origin = request.getHeaderString("Origin");
        if (origin != null && !origin.isEmpty() && corsAllowedOriginPattern.matcher(origin).matches()) {
            response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            response.getHeaders().putSingle("Access-Control-Allow-Methods", "HEAD, OPTIONS, GET, POST, PUT, DELETE");
            response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type, X-Archive-Server");
            response.getHeaders().putSingle("Access-Control-Expose-Headers", "Location");
            response.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        }
    }

}
