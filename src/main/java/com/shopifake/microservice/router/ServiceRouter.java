package com.shopifake.microservice.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Service Router configuration for Shopifake Gateway.
 * Defines all routing rules to backend microservices.
 */
@Configuration
public class ServiceRouter {

    @Value("${services.access.url:http://localhost:8081}")
    private String accessServiceUrl;

    @Value("${services.audit.url:http://localhost:8082}")
    private String auditServiceUrl;

    @Value("${services.catalog.url:http://localhost:8083}")
    private String catalogServiceUrl;

    @Value("${services.customers.url:http://localhost:8084}")
    private String customersServiceUrl;

    @Value("${services.inventory.url:http://localhost:8085}")
    private String inventoryServiceUrl;

    @Value("${services.orders.url:http://localhost:8086}")
    private String ordersServiceUrl;

    @Value("${services.pricing.url:http://localhost:8087}")
    private String pricingServiceUrl;

    @Value("${services.recommender.url:http://localhost:8088}")
    private String recommenderServiceUrl;

    @Value("${services.sales-dashboard.url:http://localhost:8089}")
    private String salesDashboardServiceUrl;

    @Value("${services.sites.url:http://localhost:8090}")
    private String sitesServiceUrl;

    @Value("${services.chatbot.url:http://localhost:8091}")
    private String chatbotServiceUrl;

    /**
     * Configures all gateway routes for the Shopifake microservices.
     * Each route maps a path pattern to a backend service URL.
     *
     * @param builder the route locator builder provided by Spring Cloud Gateway
     * @return the configured route locator with all service routes
     */
    @Bean
    public RouteLocator shopifakeRoutes(final RouteLocatorBuilder builder) {
        Map<String, String> services = Map.ofEntries(
                Map.entry("access", accessServiceUrl),
                Map.entry("audit", auditServiceUrl),
                Map.entry("catalog", catalogServiceUrl),
                Map.entry("customers", customersServiceUrl),
                Map.entry("inventory", inventoryServiceUrl),
                Map.entry("orders", ordersServiceUrl),
                Map.entry("pricing", pricingServiceUrl),
                Map.entry("recommender", recommenderServiceUrl),
                Map.entry("sales-dashboard", salesDashboardServiceUrl),
                Map.entry("sites", sitesServiceUrl),
                Map.entry("chatbot", chatbotServiceUrl)
        );

        RouteLocatorBuilder.Builder routesBuilder = builder.routes();
        
        services.forEach((serviceName, serviceUrl) -> 
            routesBuilder.route(serviceName + "-service", r -> r
                    .path("/api/" + serviceName + "/**")
                    .filters(f -> f
                            .stripPrefix(2)
                            .addRequestHeader("X-Gateway-Source", "shopifake-gateway"))
                    .uri(serviceUrl))
        );

        return routesBuilder.build();
    }
}
