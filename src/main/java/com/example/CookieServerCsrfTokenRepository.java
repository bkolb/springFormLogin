package com.example;

import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.DefaultCsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class CookieServerCsrfTokenRepository implements ServerCsrfTokenRepository {
    static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";

    static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";

    static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";

    static final String DEFAULT_CSRF_COOKIE_PATH = "/";

    private String parameterName = DEFAULT_CSRF_PARAMETER_NAME;

    private String headerName = DEFAULT_CSRF_HEADER_NAME;

    private String cookieName = DEFAULT_CSRF_COOKIE_NAME;

    private String cookiePath = DEFAULT_CSRF_COOKIE_PATH;

    private boolean cookieHttpOnly;

    private boolean cookieSecure;

    @Override
    public Mono<CsrfToken> generateToken(ServerWebExchange exchange) {
        return Mono.fromCallable(this::createCsrfToken);
    }

    @Override
    public Mono<Void> saveToken(ServerWebExchange exchange, CsrfToken token) {
        String tokenValue = token == null ? "" : token.getToken();

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, tokenValue)
                                                            .path(cookiePath)
                                                            .httpOnly(cookieHttpOnly)
                                                            .secure(cookieSecure);
        if (token == null) {
            cookieBuilder.maxAge(0L);
        } else {
            cookieBuilder.maxAge(-1);
        }

        return Mono.create(s -> {
            exchange.getResponse().addCookie(cookieBuilder.build());
            s.success();
        });
    }

    @Override
    public Mono<CsrfToken> loadToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(cookieName);
        if (cookie == null) {
            return Mono.empty();
        }

        String token = cookie.getValue();
        if (StringUtils.isEmpty(token)) {
            return Mono.empty();
        }

        return Mono.just(new DefaultCsrfToken(headerName, parameterName, token));
    }

    /**
     * Sets the parameter name that the {@link CsrfToken} is
     * expected to appear on
     * @param parameterName the new parameter name to use
     */
    public void setParameterName(String parameterName) {
        Assert.hasLength(parameterName, "parameterName cannot be null or empty");
        this.parameterName = parameterName;
    }

    /**
     * Sets the header name that the {@link CsrfToken} is expected to appear on and the
     * header that the response will contain the {@link CsrfToken}.
     *
     * @param headerName the new header name to use
     */
    public void setHeaderName(String headerName) {
        Assert.hasLength(headerName, "headerName cannot be null or empty");
        this.headerName = headerName;
    }

    /**
     * Sets the name of the cookie that the expected CSRF token is saved to and read from.
     *
     * @param cookieName the name of the cookie that the expected CSRF token is saved to
     * and read from
     */
    public void setCookieName(String cookieName) {
        Assert.hasLength(cookieName, "cookieName cannot be null or empty");
        this.cookieName = cookieName;
    }

    /**
     * Set the path that the Cookie will be created with. This will override the default functionality which uses the
     * request context as the path.
     *
     * @param cookiePath the path to use
     */
    public void setCookiePath(String cookiePath) {
        Assert.hasLength(cookiePath, "cookiePath cannot be null or empty");
        this.cookiePath = cookiePath;
    }

    /**
     * Sets the HttpOnly attribute on the cookie containing the CSRF token.
     *
     * @param cookieHttpOnly <code>true</code> sets the HttpOnly attribute, <code>false</code> does not set it.
     */
    public void setCookieHttpOnly(boolean cookieHttpOnly) {
        this.cookieHttpOnly = cookieHttpOnly;
    }

    /**
     * Sets the Secure attribute on the cookie containing the CSRF token.
     *
     * @param cookieSecure <code>true</code> sets the Secure attribute, <code>false</code> does not set it.
     */
    public void setCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }

    private CsrfToken createCsrfToken() {
        return new DefaultCsrfToken(headerName, parameterName, createNewToken());
    }

    private String createNewToken() {
        return UUID.randomUUID().toString();
    }
}
