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

import java.io.IOException;
import java.util.List;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.archive.cdxserver.auth.AllAccessAuth;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.format.cdx.CDXInputSource;
import org.archive.format.cdx.MultiCDXInputSource;
import org.archive.format.gzip.zipnum.ZipNumParams;

/**
 * Default implementation for CDX server settings.
 * <p>
 * This implementation uses Typesafe's config library for reading settings from config files.
 */
public class ConfigFileCdxServerSettings implements CdxServerSettings {

    final String ajaxAccessControl;

    final String cookieAuthToken;

    final int queryMaxLimit;

    final boolean surtMode;

    final String cdxFormat;

    final int maxPageSize;

    final boolean collapseToLast;

    final String[] noCollapsePrefix;

    final ZipNumParams defaultZipNumParams;

    final CDXInputSource cdxSource;

    final AuthChecker authChecker;

    /**
     * Constructor for all settings.
     * <p>
     * Settings are read at construction time and an unchecked exception is thrown if evaluation of
     * settings fails.
     * <p>
     * Config is read from default location.
     */
    public ConfigFileCdxServerSettings() {
        this(ConfigFactory.load());
    }

    /**
     * Constructor for all settings.
     * <p>
     * Settings are read at construction time and an unchecked exception is thrown if evaluation of
     * settings fails.
     * <p>
     * @param config the config object to use
     */
    public ConfigFileCdxServerSettings(Config config) {
        config.checkValid(ConfigFactory.defaultReference());

        ajaxAccessControl = config.getString("ajaxAccessControl");
        cookieAuthToken = config.getString("cookieAuthToken");
        queryMaxLimit = config.getInt("queryMaxLimit");
        surtMode = config.getBoolean("surtMode");
        cdxFormat = config.getString("cdxFormat");
        maxPageSize = config.getInt("maxPageSize");
        collapseToLast = config.getBoolean("collapseToLast");
        if (config.hasPath("noCollapsePrefix")) {
            noCollapsePrefix = config.getStringList("noCollapsePrefix").toArray(new String[0]);
        } else {
            noCollapsePrefix = null;
        }

        defaultZipNumParams = new ZipNumParams(config.getInt("zipNumParams.maxAggregateBlocks"),
                config.getInt("zipNumParams.maxBlocks"),
                config.getInt("zipNumParams.timestampDedupLength"),
                config.getBoolean("zipNumParams.reverse"));
        defaultZipNumParams.setSequential(config.getBoolean("zipNumParams.sequential"));

        cdxSource = parseCdxSettings(config.getConfig("cdx"));
        authChecker = parseAuthorizationSettings(config.getConfig("authorization"));
    }

    /**
     * Parse the CDX resource part of the settings.
     *
     * @param cdxConfig the configuration object containing CDX resource settings
     * @return the configured CDX source
     */
    final CDXInputSource parseCdxSettings(Config cdxConfig) {
        List<String> filenames = cdxConfig.getStringList("files");
        MultiCDXInputSource src = new MultiCDXInputSource();
        try {
            src.setCdxUris(filenames);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return src;
    }

    /**
     * Parse the authorization part of the settings.
     * <p>
     * The config object is the subtree of the configuration containing settings for authorization.
     * Only the parameter {@literal type} is required. This can be set to one of the built in
     * authorization policies (only {@code allowAll} at the moment), or to a fully qualified class
     * name. In the latter case, extra configuration can be added as parameters with the same name
     * as the corresponding property on the class. The class must implement {@link AuthChecker}
     * <p>
     * @param authConfig the configuration object containing authorization settings
     * @return the configured AuthChecker
     */
    final AuthChecker parseAuthorizationSettings(Config authConfig) {
        AuthChecker auth;
        String typeString = authConfig.getString("type");
        System.out.println("AUTH TYPE: " + typeString);
        switch (typeString) {
            case "allowAll":
                auth = new AllAccessAuth();
                break;
            default:
                auth = SettingUtil.createObject(typeString, AuthChecker.class, authConfig);
        }
        return auth;
    }

    /**
     * Get the allowed pattern for cross site ajax.
     * <p>
     * @return pattern for cross site ajax
     */
    @Override
    public String getAjaxAccessControl() {
        return ajaxAccessControl;
    }

    /**
     * Get the name of the cookie used for authorization.
     * <p>
     * @return cookie name used for authorization
     */
    @Override
    public String getCookieAuthToken() {
        return cookieAuthToken;
    }

    /**
     * Get the maximum number of CDX lines evaluated for each query.
     * <p>
     * @return max number of CDX lines parsed for each request
     */
    @Override
    public int getQueryMaxLimit() {
        return queryMaxLimit;
    }

    /**
     * Get the format expected for urlkey in CDX files.
     * <p>
     * @return true if surt mode, false if classic url
     */
    @Override
    public boolean isSurtMode() {
        return surtMode;
    }

    /**
     * Get the format expected for fields in the CDX.
     * <p>
     * At the moment only 9-field and 11-field format is supported.
     * <p>
     * @return the format of the CDX lines
     */
    @Override
    public String getCdxFormat() {
        return cdxFormat;
    }

    /**
     * Get the max page size.
     * <p>
     * @return the max page size
     */
    @Override
    public int getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * Get the collapse to last setting.
     * <p>
     * If true, timestamp-collapsing writes out the last best capture in the collapse group, instead
     * of the first.
     * <p>
     * @see DupeTimestampLastBestStatusFilter
     * @return true if collapse to last
     */
    @Override
    public boolean isCollapseToLast() {
        return collapseToLast;
    }

    /**
     * Get no collapse prefixes.
     * <p>
     * @return no collapse prefixes
     */
    @Override
    public String[] getNoCollapsePrefix() {
        return noCollapsePrefix;
    }

    /**
     * Get the default settings for ZipNum clusters.
     * <p>
     * @return the default settings for ZipNum clusters
     */
    @Override
    public ZipNumParams getDefaultZipNumParams() {
        return defaultZipNumParams;
    }

    /**
     * Get the configured CDX source.
     * <p>
     * @return the CDX source
     */
    @Override
    public CDXInputSource getCdxSource() {
        return cdxSource;
    }

    /**
     * Get the configured AuthChecker.
     * <p>
     * @return the configured AuthChecker
     */
    @Override
    public AuthChecker getAuthChecker() {
        return authChecker;
    }

}
