package com.gaofans.vertx.gateway.web.filter.factory;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.factory.AbstractGatewayFilterFactory;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.Collections;
import java.util.List;

/**
 * 移除指定的响应头
 * @author gaofans
 */
public class RemoveResponseHeaderGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig, HttpServerRequest, HttpServerResponse> {

    public RemoveResponseHeaderGatewayFilterFactory() {
        super(NameConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList(NAME_KEY);
    }

    @Override
    public GatewayFilter<HttpServerRequest, HttpServerResponse> apply(NameConfig config) {
        return (exchanger, filterChain) -> filterChain
                    .filter(exchanger)
                    .onSuccess(event -> exchanger.getResponse().headers().remove(config.getName()));
    }

}
