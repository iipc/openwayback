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
package org.netpreserve.resource.resolver.settings;

import java.util.List;
import java.util.stream.Collectors;

import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.commons.cdx.CdxSourceFactory;
import org.netpreserve.commons.cdx.cdxsource.MultiCdxSource;

/**
 * Utilities used by the settings classes.
 */
public final class SettingsUtil {

    /**
     * Private constructor to avoid instantiation.
     */
    private SettingsUtil() {
    }

    public static CdxSource createCdxSource(CdxSourceSettings settings) {
        List<CdxSource> cdxSources = settings.getIdentifiers().stream()
                .map(id -> CdxSourceFactory.getCdxSource(id))
                .filter(cdx -> cdx != null)
                .collect(Collectors.toList());

        CdxSource src;
        switch (cdxSources.size()) {
            case 0:
                throw new RuntimeException("No cdx resources configured");
            case 1:
                src = cdxSources.get(0);
                break;
            default:
                src = new MultiCdxSource();
                cdxSources.stream().forEach((s) -> {
                    ((MultiCdxSource) src).addSource(s);
                });
                break;
        }
        return src;
    }

}
