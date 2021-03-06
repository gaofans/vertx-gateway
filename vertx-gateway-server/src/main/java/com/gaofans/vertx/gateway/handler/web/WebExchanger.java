package com.gaofans.vertx.gateway.handler.web;

import com.gaofans.vertx.gateway.handler.Exchanger;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * http代理交换器
 */
public class WebExchanger implements Exchanger<HttpServerRequest, HttpServerResponse> {

    private final HttpServerRequest request;
    private final Map<String,Object> context;

    public WebExchanger(HttpServerRequest request) {
        this.request = request;
        this.context = new HashMap<>();
    }

    @Override
    public HttpServerRequest getRequest() {
        return this.request;
    }

    @Override
    public HttpServerResponse getResponse() {
        return this.request.response();
    }

    @Override
    public Map<String, Object> context() {
        return this.context;
    }
}
