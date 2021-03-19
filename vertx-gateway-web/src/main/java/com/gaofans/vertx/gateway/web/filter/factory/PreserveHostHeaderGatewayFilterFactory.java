package com.gaofans.vertx.gateway.web.filter.factory;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.factory.AbstractGatewayFilterFactory;
import com.gaofans.vertx.gateway.web.util.WebUtil;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * 是否保留host的过滤器
 */
public class PreserveHostHeaderGatewayFilterFactory extends AbstractGatewayFilterFactory<Object, HttpServerRequest, HttpServerResponse> {

    public GatewayFilter<HttpServerRequest, HttpServerResponse> apply() {
        return apply(o -> {
        });
    }

    @Override
    public GatewayFilter<HttpServerRequest, HttpServerResponse> apply(Object config) {
        return (exchanger, filterChain) -> {
            exchanger.context().put(WebUtil.PRESERVE_HOST_HEADER_ATTRIBUTE, true);
            filterChain.filter(exchanger);
        };
    }
}
