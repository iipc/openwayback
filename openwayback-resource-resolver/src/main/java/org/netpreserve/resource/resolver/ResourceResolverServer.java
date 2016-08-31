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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.accesslog.AccessLogAppender;
import org.glassfish.grizzly.http.server.accesslog.AccessLogBuilder;
import org.glassfish.grizzly.http.server.accesslog.AccessLogProbe;
import org.glassfish.grizzly.http.server.accesslog.ApacheLogFormat;
import org.glassfish.grizzly.http.server.accesslog.StreamAppender;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.netpreserve.commons.cdx.CdxSource;
import org.netpreserve.resource.resolver.settings.ServerSettings;
import org.netpreserve.resource.resolver.settings.Settings;
import org.netpreserve.resource.resolver.settings.SettingsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a Grizzly server and configures the JAX-RS application.
 */
public class ResourceResolverServer {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceResolverServer.class);

    /**
     * Construct a new ResourceResolverServer and start it.
     */
    public ResourceResolverServer() {
        Settings settings = null;

        try {
            Config config = ConfigFactory.load();
            config.checkValid(ConfigFactory.defaultReference());
            settings = ConfigBeanFactory.create(config, Settings.class);
        } catch (ConfigException ex) {
            System.err.println("Configuration error: " + ex.getLocalizedMessage());
            System.exit(1);
        }

        int port = settings.getServer().getPort();
        String connectAddress = settings.getServer().getConnectAddress();
        URI baseUri = UriBuilder.fromUri("http://" + connectAddress).port(port).build();

        CdxSource cdxSource = SettingsUtil.createCdxSource(settings.getCdxSource());

        ResourceConfig config = ResourceConfig.forApplication(new ApplicationConfig(settings, cdxSource));
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config, false);
        enableAccessLog(settings.getServer(), server);
        try {
            server.start();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        LOG.info("Resource Resolver (v. {}) started",
                ResourceResolverServer.class.getPackage().getImplementationVersion());
    }

    /**
     * Configure access logging.
     * <p>
     * @param settings the configuration used for setting up the logging
     * @param httpServer the Grizzly server instance to configure
     */
    private void enableAccessLog(ServerSettings settings, HttpServer httpServer) {
        if (settings.getAccessLog() == null) {
            return;
        }

        switch (settings.getAccessLog()) {
            case "console":
                AccessLogAppender appender = new StreamAppender(System.out);
                AccessLogProbe alp = new AccessLogProbe(appender, ApacheLogFormat.COMBINED,
                        AccessLogProbe.DEFAULT_STATUS_THRESHOLD);
                httpServer.getServerConfiguration().getMonitoringConfig().getWebServerConfig().addProbes(alp);
                break;
            case "file":
                File logFile = new File(settings.getAccessLogFileName());
                if (System.getProperty("app.home") != null && !logFile.isAbsolute()) {
                    logFile = new File(System.getProperty("app.home"), settings.getAccessLogFileName());
                }
                AccessLogBuilder logBuilder = new AccessLogBuilder(logFile);
                if (settings.getAccessLogArchivePattern() != null
                        && !settings.getAccessLogArchivePattern().isEmpty()) {
                    logBuilder.rotationPattern(settings.getAccessLogArchivePattern());
                }
                logBuilder.instrument(httpServer.getServerConfiguration());
                break;
            default:
                // For all other values, do not enable access log
                return;
        }

    }

}
