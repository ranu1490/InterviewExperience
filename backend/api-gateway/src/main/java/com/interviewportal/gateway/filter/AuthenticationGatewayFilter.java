package com.interviewportal.gateway.filter;

import com.interviewportal.common.security.JwtService;
import com.interviewportal.common.security.SecurityHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Edge authentication for every request that passes through the gateway.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Validate the {@code Authorization: Bearer <jwt>} header when present.</li>
 *   <li>Reject invalid/expired tokens early (before they waste downstream capacity).</li>
 *   <li>Enforce the coarse rule "anonymous users may only issue safe GET reads and hit the
 *       public auth endpoints"; all writes require a valid token.</li>
 *   <li>Forward the authenticated identity to downstream services as trusted {@code X-User-*}
 *       headers so they don't have to re-parse the token (they still can, for defence in depth).</li>
 * </ul>
 *
 * <p>Why do this at the edge? Fail-fast rejection of unauthenticated writes protects the whole
 * fleet and keeps each service's own security config focused on fine-grained rules (ownership,
 * roles). Alternative: validate only inside each service — simpler but wastes gateway/network
 * hops on requests that were doomed to fail.
 */
@Component
public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationGatewayFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public AuthenticationGatewayFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = extractToken(request);

        if (token == null) {
            // No credentials: only anonymous, safe reads are allowed.
            if (method == HttpMethod.GET || method == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }
            return unauthorized(exchange, "Authentication required");
        }

        if (!jwtService.isValid(token)) {
            return unauthorized(exchange, "Invalid or expired token");
        }

        Long userId = jwtService.extractUserId(token);
        List<String> roles = jwtService.extractRoles(token);
        String username = jwtService.extractUsername(token);
        String email = jwtService.extractEmail(token);

        ServerHttpRequest mutated = request.mutate()
                .header(SecurityHeaders.USER_ID, String.valueOf(userId))
                .header(SecurityHeaders.USERNAME, username)
                .header(SecurityHeaders.USER_EMAIL, email)
                .header(SecurityHeaders.USER_ROLES, String.join(",", roles))
                .build();

        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth")
                || path.startsWith("/oauth2")
                || path.startsWith("/login/oauth2")
                || path.startsWith("/actuator")
                || path.contains("/swagger")
                || path.contains("/v3/api-docs");
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        log.debug("Rejecting request to {}: {}", exchange.getRequest().getURI().getPath(), reason);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run early, before routing.
        return -100;
    }
}
