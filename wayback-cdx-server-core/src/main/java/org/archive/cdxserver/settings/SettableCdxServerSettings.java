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

import org.archive.cdxserver.auth.AllAccessAuth;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.format.cdx.CDXInputSource;
import org.archive.format.gzip.zipnum.ZipNumParams;

/**
 * CDX server settings implementation which allows setting all the configuration options with setter
 * methods. Useful for testing.
 */
public class SettableCdxServerSettings implements CdxServerSettings {

    String ajaxAccessControl = "";

    String cookieAuthToken = "cdx_auth_token";

    int queryMaxLimit = 150000;

    boolean surtMode = true;

    String cdxFormat = "cdx09";

    int maxPageSize = 1;

    boolean collapseToLast = false;

    String[] noCollapsePrefix;

    ZipNumParams defaultZipNumParams = new ZipNumParams(1, 1, 0, false);

    CDXInputSource cdxSource;

    AuthChecker authChecker = new AllAccessAuth();

    @Override
    public String getAjaxAccessControl() {
        return ajaxAccessControl;
    }

    @Override
    public AuthChecker getAuthChecker() {
        return authChecker;
    }

    @Override
    public String getCdxFormat() {
        return cdxFormat;
    }

    @Override
    public CDXInputSource getCdxSource() {
        return cdxSource;
    }

    @Override
    public String getCookieAuthToken() {
        return cookieAuthToken;
    }

    @Override
    public ZipNumParams getDefaultZipNumParams() {
        return defaultZipNumParams;
    }

    @Override
    public int getMaxPageSize() {
        return maxPageSize;
    }

    @Override
    public String[] getNoCollapsePrefix() {
        return noCollapsePrefix;
    }

    @Override
    public int getQueryMaxLimit() {
        return queryMaxLimit;
    }

    @Override
    public boolean isCollapseToLast() {
        return collapseToLast;
    }

    @Override
    public boolean isSurtMode() {
        return surtMode;
    }

    /**
     * Set the allowed pattern for cross site ajax.
     * <p>
     * @param ajaxAccessControl the host pattern for cross site ajax
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setAjaxAccessControl(String ajaxAccessControl) {
        this.ajaxAccessControl = ajaxAccessControl;
        return this;
    }

    /**
     * Set the name of the cookie used for authorization.
     * <p>
     * @param cookieAuthToken cookie name used for authorization
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setCookieAuthToken(String cookieAuthToken) {
        this.cookieAuthToken = cookieAuthToken;
        return this;
    }

    /**
     * Set the maximum number of CDX lines evaluated for each query.
     * <p>
     * @param queryMaxLimit max number of CDX lines parsed for each request
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setQueryMaxLimit(int queryMaxLimit) {
        this.queryMaxLimit = queryMaxLimit;
        return this;
    }

    /**
     * Set the format expected for urlkey in CDX files.
     * <p>
     * @param surtMode true if surt mode, false if classic url
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setSurtMode(boolean surtMode) {
        this.surtMode = surtMode;
        return this;
    }

    /**
     * Set the format expected for fields in the CDX.
     * <p>
     * For now the values {@code cdx09} and {@code cdx11} are allowed.
     * <p>
     * @param cdxFormat the name of teh format to use
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setCdxFormat(String cdxFormat) {
        this.cdxFormat = cdxFormat;
        return this;
    }

    /**
     * Set the max page size.
     * <p>
     * @param maxPageSize the maximum page size
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setMaxPageSize(int maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Set collapse to last.
     * <p>
     * If true, timestamp-collapsing writes out the last best capture in the collapse group, instead
     * of the first.
     * <p>
     * @param collapseToLast set to true for last best capture
     * @return this object to allow method call chaining
     * @see DupeTimestampLastBestStatusFilter
     */
    public SettableCdxServerSettings setCollapseToLast(boolean collapseToLast) {
        this.collapseToLast = collapseToLast;
        return this;
    }

    /**
     * Set no collapse prefixes.
     * <p>
     * @param noCollapsePrefix no collapse prefixes
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setNoCollapsePrefix(String[] noCollapsePrefix) {
        this.noCollapsePrefix = noCollapsePrefix;
        return this;
    }

    /**
     * Set the default settings for ZipNum clusters.
     * <p>
     * @param defaultZipNumParams the default ZipNum parameters
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setDefaultZipNumParams(ZipNumParams defaultZipNumParams) {
        this.defaultZipNumParams = defaultZipNumParams;
        return this;
    }

    /**
     * Set the configured CDX source.
     * <p>
     * @param cdxSource the CDX Input Source
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setCdxSource(CDXInputSource cdxSource) {
        this.cdxSource = cdxSource;
        return this;
    }

    /**
     * Set the configured AuthChecker used for access control.
     * <p>
     * @param authChecker the AuthChecker instance
     * @return this object to allow method call chaining
     */
    public SettableCdxServerSettings setAuthChecker(AuthChecker authChecker) {
        this.authChecker = authChecker;
        return this;
    }

}
