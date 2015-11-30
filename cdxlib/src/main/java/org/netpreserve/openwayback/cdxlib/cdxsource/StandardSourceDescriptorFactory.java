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
package org.netpreserve.openwayback.cdxlib.cdxsource;

import java.io.IOException;

import org.netpreserve.openwayback.cdxlib.SourceDescriptorFactory;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

import org.netpreserve.openwayback.cdxlib.CdxLineSchema;

/**
 *
 */
public class StandardSourceDescriptorFactory extends SourceDescriptorFactory {

    @Override
    public SourceDescriptor createSourceDescriptor(String type, URI location,
            Map<String, Object> params) {

        switch (type) {
            case "cdxfile":
                try {
                    if (location.getScheme() == null) {
                        return new CdxFileDescriptor(Paths.get(location.getPath()));
                    }
                    if (location.getScheme().equals("file")) {
                        return new CdxFileDescriptor(Paths.get(location));
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                break;

            case "zipnum":
                if (location.getScheme() == null || location.getScheme().equals("file")) {
                    return new ZipnumDescriptor(Paths.get(location),
                            (CdxLineSchema) params.get("inputFormat"));
                }
                break;

            default:
                break;
        }
        return null;
    }

}
