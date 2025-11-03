package com.shopifake.microservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

/**
 * Global filter for logging incoming requests and responses through the gateway.
 * Logs request details, response status, and execution time.
 */
@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant startTime = Instant.now();
        String requestId = exchange.getRequest().getId();
        
        log.info("[{}] Incoming request: {} {} from {}",
                requestId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI(),
                exchange.getRequest().getRemoteAddress());

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    Duration duration = Duration.between(startTime, Instant.now());
                    log.info("[{}] Request completed: {} {} - Status: {} - Duration: {}ms",
                            requestId,
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI(),
                            exchange.getResponse().getStatusCode(),
                            duration.toMillis());
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
