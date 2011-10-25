/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.liveweb;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.ReflectionSocketFactory;
import org.archive.wayback.webapp.PerformanceLogger;

/**
 * ProtocolSocketFactory which logs the amount of time taked to do DNS lookups.
 * 
 * @author brad
 *
 */
public class DNSTimingProtocolSocketFactory extends DefaultProtocolSocketFactory {

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory#createSocket(java.lang.String, int)
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		long start = System.currentTimeMillis();
		InetAddress addr = InetAddress.getByName(host);
		long end = System.currentTimeMillis();
		PerformanceLogger.noteElapsed("DNS:" + host, end - start);
		return new Socket(addr, port);
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int, org.apache.commons.httpclient.params.HttpConnectionParams)
	 */
	@Override
    public Socket createSocket(
            final String host,
            final int port,
            final InetAddress localAddress,
            final int localPort,
            final HttpConnectionParams params
        ) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return createSocket(host, port, localAddress, localPort);
        } else {
            // To be eventually deprecated when migrated to Java 1.4 or above
            Socket socket = ReflectionSocketFactory.createSocket(
                "javax.net.SocketFactory", host, port, localAddress, localPort, timeout);
            if (socket == null) {
                socket = ControllerThreadSocketFactory.createSocket(
                    this, host, port, localAddress, localPort, timeout);
            }
            return socket;
        }
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
	 */
	@Override
    public Socket createSocket(
            String host,
            int port,
            InetAddress localAddress,
            int localPort
        ) throws IOException, UnknownHostException {
			long start = System.currentTimeMillis();
			InetAddress addr = InetAddress.getByName(host);
			long end = System.currentTimeMillis();
			PerformanceLogger.noteElapsed("DNS:" + host, end - start);
			return new Socket(addr,port,localAddress,localPort);
        }

}
