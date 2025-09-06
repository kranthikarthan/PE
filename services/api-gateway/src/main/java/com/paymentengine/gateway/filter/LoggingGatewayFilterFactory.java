package com.paymentengine.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * Custom logging filter factory for specific route logging
 */
@Component
public class LoggingGatewayFilterFactory 
    extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingGatewayFilterFactory.class);
    
    public LoggingGatewayFilterFactory() {
        super(Config.class);
    }
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (config.isEnabled()) {
                String routeId = exchange.getAttribute("org.springframework.cloud.gateway.support.RouteDefinitionRouteLocator.routeDefinition") != null
                    ? exchange.getAttribute("org.springframework.cloud.gateway.support.RouteDefinitionRouteLocator.routeDefinition").toString()
                    : "unknown";
                
                logger.info("Route logging - Route: {}, Method: {}, URI: {}, Message: {}",
                    routeId,
                    exchange.getRequest().getMethod(),
                    exchange.getRequest().getURI(),
                    config.getMessage());
            }
            
            return chain.filter(exchange);
        };
    }
    
    public static class Config {
        private boolean enabled = true;
        private String message = "Route accessed";
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}