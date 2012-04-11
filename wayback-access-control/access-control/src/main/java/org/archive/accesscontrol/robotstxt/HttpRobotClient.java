package org.archive.accesscontrol.robotstxt;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.archive.accesscontrol.RobotsUnavailableException;

/**
 * HttpRobotClient allows fetching of robots.txt rules over HTTP.
 * 
 * @author aosborne
 *
 */
public class HttpRobotClient extends RobotClient {
    private static final Logger LOGGER = Logger.getLogger(
            RobotClient.class.getName());
    protected HttpClient http = new HttpClient(
            new MultiThreadedHttpConnectionManager());
    
    public HttpClient getHttpClient() {
        return http;
    }

    public RobotRules getRulesForUrl(String url, String userAgent) throws IOException, RobotsUnavailableException {
        String robotsUrl = robotsUrlForUrl(url);
        HttpMethod method = new GetMethod(robotsUrl);
        method.addRequestHeader("User-Agent", userAgent);
        try {
            int code = http.executeMethod(method);
            // TODO: Constant 200
            if (code != 200) {
                throw new RobotsUnavailableException(robotsUrl);
            }
        } catch (HttpException e) {
            e.printStackTrace();
            throw new RobotsUnavailableException(robotsUrl);
        } catch (UnknownHostException e) {
            LOGGER.info("Unknown host for URL " + robotsUrl);
            throw new RobotsUnavailableException(robotsUrl);
        } catch (ConnectTimeoutException e) {
            LOGGER.info("Connection Timeout for URL " + robotsUrl);
            throw new RobotsUnavailableException(robotsUrl);
        } catch (NoRouteToHostException e) {
            LOGGER.info("No route to host for URL " + robotsUrl);
            throw new RobotsUnavailableException(robotsUrl);
        } catch (ConnectException e) {
            LOGGER.info("ConnectException URL " + robotsUrl);
            throw new RobotsUnavailableException(robotsUrl);
        }
        RobotRules rules = new RobotRules();
        rules.parse(method.getResponseBodyAsStream());
        return rules;
    }

    @Override
    public void prepare(Collection<String> urls, String userAgent) {
        // no-op
    }

    @Override
    public void setRobotProxy(String host, int port) {
        http.getHostConfiguration().setProxy(host, port);
    }
}
