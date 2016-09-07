/*
 * Copyright 2016 The International Internet Preservation Consortium.
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
 * Web server settings.
 */
public class ServerSettings {

    private String connectAddress;

    private int port;

    private String accessLog;

    private String accessLogFileName;

    private String accessLogArchivePattern;

    /**
     * Get the address to listen to.
     * <p>
     * The address identifies which interface on the host to listen to for incoming requests. A value of 0.0.0.0 means
     * listen to all addresses configured on this host.
     * <p>
     * @return the address to listen to
     */
    public String getConnectAddress() {
        return connectAddress;
    }

    public void setConnectAddress(String connectAddress) {
        this.connectAddress = connectAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAccessLog() {
        return accessLog;
    }

    public void setAccessLog(String accessLog) {
        this.accessLog = accessLog;
    }

    public String getAccessLogFileName() {
        return accessLogFileName;
    }

    public void setAccessLogFileName(String accessLogFileName) {
        this.accessLogFileName = accessLogFileName;
    }

    public String getAccessLogArchivePattern() {
        return accessLogArchivePattern;
    }

    public void setAccessLogArchivePattern(String accessLogArchivePattern) {
        this.accessLogArchivePattern = accessLogArchivePattern;
    }

}
