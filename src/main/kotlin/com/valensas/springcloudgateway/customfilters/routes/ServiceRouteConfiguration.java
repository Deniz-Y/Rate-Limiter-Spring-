package com.valensas.springcloudgateway.customfilters.routes;

import com.valensas.springcloudgateway.customfilters.filters.factories.LoggingGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

public class ServiceRouteConfiguration {
    @Bean
    public RouteLocator routes(
            RouteLocatorBuilder builder,
            LoggingGatewayFilterFactory loggingFactory) {
        return builder.routes()
                .route("service_route_java_config", r -> r.path("/service/**")
                        .filters(f ->
                                f.rewritePath("/service(?<segment>/?.*)", "$\\{segment}")
                                        .filter(loggingFactory.apply(
                                                new LoggingGatewayFilterFactory.Config("My Custom Message", true, true))))
                        .uri("http://localhost:8081"))
                .build();
    }
}
