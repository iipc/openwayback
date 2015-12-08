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

import java.net.URI;
import java.util.Map;
import java.util.ServiceLoader;

import org.netpreserve.openwayback.cdxlib.cdxsource.SourceDescriptor;

/**
 * SourceDescriptorFactory using a ServiceLoader so that new SourceDescriptors could be plugged in
 * by adding a jar file.
 */
public abstract class SourceDescriptorFactory {

    private static final ServiceLoader<SourceDescriptorFactory> SERVICE_LOADER
            = ServiceLoader.load(SourceDescriptorFactory.class);

    /**
     * Create a new SourceDescriptor.
     *
     * @param type a string describing the type of resource
     * @param location where to find the resource
     * @param params optional parameters needed for some SourceDescriptors
     * @return a matching source descriptor or null if none could be found
     */
    public static final SourceDescriptor getDescriptor(String type, URI location,
            Map<String, Object> params) {

        for (SourceDescriptorFactory sdf : SERVICE_LOADER) {
            SourceDescriptor sd = sdf.createSourceDescriptor(type, location, params);
            if (sd != null) {
                return sd;
            }
        }
        return null;
    }

    public abstract SourceDescriptor createSourceDescriptor(String type, URI location,
            Map<String, Object> params);

}
