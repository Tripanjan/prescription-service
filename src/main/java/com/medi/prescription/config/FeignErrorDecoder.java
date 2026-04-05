package com.medi.prescription.config;

import com.medi.prescription.exception.ResourceNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Error Decoder for Feign Client
 * 
 * This class intercepts HTTP error responses from remote services
 * and converts them into appropriate exceptions.
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign error occurred: method={}, status={}, reason={}", 
                  methodKey, response.status(), response.reason());
        
        // Handle different HTTP status codes
        switch (response.status()) {
            case 404:
                // Not Found - convert to ResourceNotFoundException
                return new ResourceNotFoundException(
                    "Resource not found in Medicine Service: " + response.reason()
                );
                
            case 400:
                // Bad Request
                return new IllegalArgumentException(
                    "Invalid request to Medicine Service: " + response.reason()
                );
                
            case 503:
                // Service Unavailable
                return new RuntimeException(
                    "Medicine Service is currently unavailable. Please try again later."
                );
                
            case 500:
                // Internal Server Error
                return new RuntimeException(
                    "Medicine Service encountered an internal error: " + response.reason()
                );
                
            default:
                // Use default decoder for other errors
                return defaultDecoder.decode(methodKey, response);
        }
    }
}
