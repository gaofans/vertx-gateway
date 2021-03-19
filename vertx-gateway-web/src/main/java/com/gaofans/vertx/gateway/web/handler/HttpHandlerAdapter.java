package com.gaofans.vertx.gateway.web.handler;

import com.gaofans.vertx.gateway.web.HttpRouteLocator;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

/**
 * vertx web适配器
 * @author GaoFans
 * @since 2021/2/26
 */
public class HttpHandlerAdapter implements Handler<HttpServerRequest> {

    private final HttpFilteringHandler filteringHandler;

    public HttpHandlerAdapter(HttpFilteringHandler filteringHandler) {
        this.filteringHandler = filteringHandler;
    }

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        filteringHandler.handle(new HttpExchanger(httpServerRequest,new HttpRouteLocator()));
    }
}
