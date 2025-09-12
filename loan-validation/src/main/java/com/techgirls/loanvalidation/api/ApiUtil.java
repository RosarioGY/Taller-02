package com.techgirls.loanvalidation.api;

import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Utility class for OpenAPI generated controllers.
 * Provides helper methods for handling API responses.
 */
public class ApiUtil {

    /**
     * Sets an example response for the given exchange.
     * This is typically used by OpenAPI generated code for mock responses.
     * 
     * @param exchange the server web exchange
     * @param mediaType the media type of the response
     * @param example the example response body as string
     * @return an empty Mono
     */
    public static Mono<Void> getExampleResponse(ServerWebExchange exchange, MediaType mediaType, String example) {
        return exchange.getResponse().writeWith(
            Mono.just(exchange.getResponse()
                .bufferFactory()
                .wrap(example.getBytes()))
        );
    }
}