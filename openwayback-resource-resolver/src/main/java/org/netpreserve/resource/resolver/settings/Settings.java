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

/**
 * Settings for the Resource Resolver.
 */
public class Settings {

    private String corsAllowedOriginPattern = "";

    private String cookieAuthToken = "cdx_auth_token";

    private int queryMaxLimit = 150000;

    private ServerSettings serverSettings;

    private CdxSourceSettings cdxSourceSettings;

    private boolean logTraffic;

    /**
     * Get the allowed pattern for cross site ajax.
     * <p>
     * @return pattern for cross site ajax
     */
    public String getCorsAllowedOriginPattern() {
        return corsAllowedOriginPattern;
    }

    /**
     * Get the name of the cookie used for authorization.
     * <p>
     * @return cookie name used for authorization
     */
    public String getCookieAuthToken() {
        return cookieAuthToken;
    }

    /**
     * Get the maximum number of CDX lines evaluated for each query.
     * <p>
     * @return max number of CDX lines parsed for each request
     */
    public int getQueryMaxLimit() {
        return queryMaxLimit;
    }

    /**
     * Set the allowed pattern for cross site ajax.
     * <p>
     * @param corsAllowedOriginPattern the host pattern for cross site ajax
     */
    public void setCorsAllowedOriginPattern(String corsAllowedOriginPattern) {
        this.corsAllowedOriginPattern = corsAllowedOriginPattern;
    }

    /**
     * Set the name of the cookie used for authorization.
     * <p>
     * @param cookieAuthToken cookie name used for authorization
     */
    public void setCookieAuthToken(String cookieAuthToken) {
        this.cookieAuthToken = cookieAuthToken;
    }

    /**
     * Set the maximum number of CDX lines evaluated for each query.
     * <p>
     * @param queryMaxLimit max number of CDX lines parsed for each request
     */
    public void setQueryMaxLimit(int queryMaxLimit) {
        this.queryMaxLimit = queryMaxLimit;
    }

    /**
     * Should traffic be logged.
     * <p>
     * This is not the access log, but logging of request and response including the headers.
     * <p>
     * @return true if traffic shoud be logged.
     */
    public boolean isLogTraffic() {
        return logTraffic;
    }

    /**
     * Should traffic be logged.
     * <p>
     * This is not the access log, but logging of request and response including the headers.
     * <p>
     * @param logTraffic true if traffic shoud be logged.
     */
    public void setLogTraffic(boolean logTraffic) {
        this.logTraffic = logTraffic;
    }

    /**
     * Get server specific settings.
     * <p>
     * This is only applicable when used as a self-contained application.
     * <p>
     * @return the server specific settings
     */
    public ServerSettings getServer() {
        return serverSettings;
    }

    /**
     * Set server specific settings.
     * <p>
     * This is only applicable when used as a self-contained application.
     * <p>
     * @param serverSettings the server specific settings
     */
    public void setServer(ServerSettings serverSettings) {
        this.serverSettings = serverSettings;
    }

    /**
     * Get CDX source settings.
     * <p>
     * @param cdxSourceSettings the cdx source settings
     */
    public void setCdxSource(CdxSourceSettings cdxSourceSettings) {
        this.cdxSourceSettings = cdxSourceSettings;
    }

    /**
     * Set CDX source settings.
     * <p>
     * @return the CDX source settings
     */
    public CdxSourceSettings getCdxSource() {
        return cdxSourceSettings;
    }

}
