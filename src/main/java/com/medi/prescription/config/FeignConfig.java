package com.medi.prescription.config;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign Client Configuration
 * 
 * This class provides custom configuration for Feign clients:
 * - Logger level for detailed request/response logging
 * - Custom error decoder for handling service errors
 */
@Configuration
public class FeignConfig {
    
    /**
     * Logger level for Feign clients
     * 
     * NONE: No logging (default)
     * BASIC: Log only request method, URL, response status, and execution time
     * HEADERS: Log request/response headers along with BASIC info
     * FULL: Log headers, body, and metadata for both requests and responses
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    /**
     * Custom error decoder for Feign clients
     * 
     * Handles HTTP errors from remote services and converts them to
     * appropriate exceptions. This gives better control over error responses.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }
}
