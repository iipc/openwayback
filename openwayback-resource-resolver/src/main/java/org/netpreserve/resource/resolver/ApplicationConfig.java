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
package org.netpreserve.resource.resolver;

import java.util.HashMap;
import java.util.Map;

import org.netpreserve.resource.resolver.resources.LookupResource;
import org.netpreserve.resource.resolver.jaxrs.CrossOriginResourceSharingFilter;
import org.netpreserve.resource.resolver.jaxrs.SettingsProvider;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.resource.resolver.jaxrs.CdxSourceProvider;
import org.netpreserve.resource.resolver.jaxrs.CdxjHtmlWriter;
import org.netpreserve.resource.resolver.jaxrs.CdxjWriter;
import org.netpreserve.resource.resolver.jaxrs.DateRangeParamConverterProvider;
import org.netpreserve.resource.resolver.jaxrs.LegacyCdxWriter;
import org.netpreserve.resource.resolver.jaxrs.UriMatchTypeParamConverterProvider;
import org.netpreserve.resource.resolver.jaxrs.VersionHeaderFilter;
import org.netpreserve.resource.resolver.resources.ListResource;
import org.netpreserve.resource.resolver.settings.Settings;

/**
 * Jersey application configuration.
 * <p>
 * Defines the components of the JAX-RS application and supplies additional meta-data.
 */
@ApplicationPath("/")
public class ApplicationConfig extends Application {
    private final Settings settings;
    private final CdxSource cdxSource;

    public ApplicationConfig(Settings settings, CdxSource cdxSource) {
        this.settings = settings;
        this.cdxSource = cdxSource;
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();

        resources.add(DateRangeParamConverterProvider.class);
        resources.add(UriMatchTypeParamConverterProvider.class);
        resources.add(CdxjHtmlWriter.class);
        resources.add(CdxjWriter.class);
        resources.add(LegacyCdxWriter.class);
        resources.add(LookupResource.class);
        resources.add(ListResource.class);
        resources.add(VersionHeaderFilter.class);
        resources.add(CrossOriginResourceSharingFilter.class);
        resources.add(EncodingFilter.class);
        resources.add(GZipEncoder.class);
        resources.add(DeflateEncoder.class);

        return resources;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> resources = new java.util.HashSet<>();

        resources.add(new SettingsProvider(settings));
        resources.add(new CdxSourceProvider(cdxSource));

        return resources;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();

        if (settings.isLogTraffic()) {
            props.put(LoggingFeature.LOGGING_FEATURE_LOGGER_NAME, "org.netpreserve.resource.resolver.traffic");
            props.put(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL_SERVER, "FINE");
            props.put(LoggingFeature.LOGGING_FEATURE_VERBOSITY_SERVER, LoggingFeature.Verbosity.HEADERS_ONLY);
        }

        return props;
    }

}
