package org.archive.cdxserver;

import org.archive.cdxserver.settings.ConfigFileCdxServerSettings;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.archive.cdxserver.auth.AuthChecker;

import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.settings.CdxServerSettings;
import org.archive.url.UrlSurtRangeComputer;
import org.archive.url.WaybackURLKeyMaker;
import org.springframework.stereotype.Controller;

@Controller
public class BaseCDXServer {

    final CdxServerSettings settings;

    protected UrlSurtRangeComputer urlSurtRangeComputer;

    protected WaybackURLKeyMaker canonicalizer = null;

    public BaseCDXServer() {
        this(new ConfigFileCdxServerSettings());
    }

    public BaseCDXServer(final CdxServerSettings settings) {
        this.settings = settings;
        canonicalizer = new WaybackURLKeyMaker(settings.isSurtMode());
        urlSurtRangeComputer = new UrlSurtRangeComputer(settings.isSurtMode());
    }

    public String getCookieAuthToken() {
        return settings.getCookieAuthToken();
    }

    public String canonicalize(String url, boolean surt) throws UnsupportedEncodingException, URISyntaxException {
        if ((canonicalizer == null) || (url == null) || url.isEmpty()) {
            return url;
        }

        url = java.net.URLDecoder.decode(url, "UTF-8");

        if (surt) {
            return url;
        }

        int slashIndex = url.indexOf('/');
        // If true, assume this is already a SURT and skip
        if ((slashIndex > 0) && url.charAt(slashIndex - 1) == ')') {
            return url;
        }

        return canonicalizer.makeKey(url);
    }

    protected void prepareResponse(HttpServletResponse response) {
        response.setContentType("text/plain; charset=\"UTF-8\"");
    }

    protected void handleAjax(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");

        if (origin == null) {
            return;
        }

        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", origin);
    }

    public AuthChecker getAuthChecker() {
        return settings.getAuthChecker();
    }

    protected AuthToken createAuthToken(HttpServletRequest request) {
        return new AuthToken(extractAuthToken(request, settings.getCookieAuthToken()));
    }

    protected String extractAuthToken(HttpServletRequest request, String cookieAuthToken) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieAuthToken)) {
                return cookie.getValue();
            }
        }

        return null;
    }

}
