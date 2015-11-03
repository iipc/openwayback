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
package org.archive.cdxserver.settings;

import org.archive.cdxserver.auth.AuthChecker;
import org.archive.format.cdx.CDXInputSource;
import org.archive.format.gzip.zipnum.ZipNumParams;

/**
 * Configuration options for CDX server.
 */
public interface CdxServerSettings {

    /**
     * Get the allowed pattern for cross site ajax.
     * <p>
     * @return pattern for cross site ajax
     */
    String getAjaxAccessControl();

    /**
     * Get the configured AuthChecker.
     * <p>
     * @return the configured AuthChecker
     */
    AuthChecker getAuthChecker();

    /**
     * Get the format expected for fields in the CDX.
     * <p>
     * At the moment only 9-field and 11-field format is supported.
     * <p>
     * @return the format of the CDX lines
     */
    String getCdxFormat();

    /**
     * Get the configured CDX source.
     * <p>
     * @return the CDX source
     */
    CDXInputSource getCdxSource();

    /**
     * Get the name of the cookie used for authorization.
     * <p>
     * @return cookie name used for authorization
     */
    String getCookieAuthToken();

    /**
     * Get the default settings for ZipNum clusters.
     * <p>
     * @return the default settings for ZipNum clusters
     */
    ZipNumParams getDefaultZipNumParams();

    /**
     * Get the max page size.
     * <p>
     * @return the max page size
     */
    int getMaxPageSize();

    /**
     * Get no collapse prefixes.
     * <p>
     * @return no collapse prefixes
     */
    String[] getNoCollapsePrefix();

    /**
     * Get the maximum number of CDX lines evaluated for each query.
     * <p>
     * @return max number of CDX lines parsed for each request
     */
    int getQueryMaxLimit();

    /**
     * Get the collapse to last setting.
     * <p>
     * If true, timestamp-collapsing writes out the last best capture in the collapse group, instead
     * of the first.
     * <p>
     * @see DupeTimestampLastBestStatusFilter
     * @return true if collapse to last
     */
    boolean isCollapseToLast();

    /**
     * Get the format expected for urlkey in CDX files.
     * <p>
     * @return true if surt mode, false if classic url
     */
    boolean isSurtMode();

}
