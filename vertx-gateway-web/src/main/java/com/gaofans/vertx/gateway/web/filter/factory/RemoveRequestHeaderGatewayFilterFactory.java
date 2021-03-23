package com.gaofans.vertx.gateway.web.filter.factory;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.factory.AbstractGatewayFilterFactory;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public class RemoveRequestHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<Object, HttpServerRequest, HttpServerResponse> {
    @Override
    public GatewayFilter<HttpServerRequest, HttpServerResponse> apply(Object config) {
        return null;
    }
}
