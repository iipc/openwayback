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
 *
 */
public abstract class SourceDescriptorFactory {

    private static final ServiceLoader<SourceDescriptorFactory> sourceDescriptorLoader
            = ServiceLoader.load(SourceDescriptorFactory.class);

    public static SourceDescriptor getDescriptor(String type, URI location,
            Map<String, Object> params) {

        for (SourceDescriptorFactory sdf : sourceDescriptorLoader) {
            SourceDescriptor sd = sdf.createSourceDescriptor(type, location, params);
            if (sd != null) {
                return sd;
            }
        }
        return null;
    }

    public abstract SourceDescriptor createSourceDescriptor(String type, URI location, Map<String,
            Object> params);
}
