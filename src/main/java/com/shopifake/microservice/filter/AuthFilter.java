package com.shopifake.microservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Gateway filter for JWT AUTHENTICATION and role injection into the header.
 * This filter verifies identity and transmits the role; authorization (RBAC) is handled
 * by the destination microservice.
 */
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    // Secret key used for token signature, retrieved from application.yml
    @Value("${security.jwt.secret}")
    private String jwtSecret;

    private static final String ROLE_CLAIM = "role";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String ROLE_HEADER = "X-User-Role";

    // --- NOTE: Centralized authorization rules (ACCESS_RULES) have been removed
    // to favor the distributed authorization model (RBAC handled by each microservice).

    public AuthFilter() {
        super(Config.class);
    }

    public static class Config {
        // Empty constructor required for GraalVM native build
        public Config() {
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // 1. CHECK FOR PRESENCE OF AUTHORIZATION HEADER
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return this.onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7).trim();

            try {
                // 2. AUTHENTICATION: VALIDATE JWT TOKEN
                Claims claims = validateToken(token);
                
                String userId = claims.get("id", String.class);
                String userRole = claims.get(ROLE_CLAIM, String.class);
                
                if (userRole == null || userRole.isEmpty()) {
                    return this.onError(exchange, "Role claim missing in token", HttpStatus.UNAUTHORIZED);
                }

                // 3. IDENTITY INJECTION: ADD USER ID AND ROLE TO HEADERS
                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(originalRequest -> originalRequest
                        .header(USER_ID_HEADER, userId) 
                        .header(ROLE_HEADER, userRole) 
                        // Note: The JWT token is not forwarded to the microservice.
                    )
                    .build();

                // 4. ROUTING: Continue the chain to the microservice
                return chain.filter(modifiedExchange);

            } catch (JwtException e) {
                // Invalid token (signature, expiration, etc.)
                return this.onError(exchange, "JWT validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                return this.onError(exchange, "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        };
    }
    
    /**
     * Validates the token using the configured secret key.
     * This function performs authentication.
     */
    private Claims validateToken(String token) throws JwtException {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        System.err.println("Authentication Error: " + err);
        String responseBody = String.format("{\"error\": \"%s\", \"status\": %d}", httpStatus.getReasonPhrase(), httpStatus.value());
        
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse().bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8)))
        );
    }
}