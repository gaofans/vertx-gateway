package com.gaofans.vertx.gateway.web.handler;

import com.gaofans.vertx.gateway.route.Route;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * vertx web适配器
 * @author GaoFans
 * @since 2021/2/26
 */
public class VertxHandlerAdapter implements Handler<HttpServerRequest> {

    private final WebFilteringHandler filteringHandler;

    public VertxHandlerAdapter(WebFilteringHandler filteringHandler) {
        this.filteringHandler = filteringHandler;
    }

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        filteringHandler.handle(new WebExchanger(httpServerRequest,() -> {
            List<Route<HttpServerRequest, HttpServerResponse>> routes = new ArrayList<>();
            route.
            return routes;
        }));
    }
}
