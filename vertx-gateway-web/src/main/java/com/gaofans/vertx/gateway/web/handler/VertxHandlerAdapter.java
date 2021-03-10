package com.gaofans.vertx.gateway.web.handler;

import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.web.predicate.PathRoutePredicateFactory;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.ArrayList;
import java.util.Collections;
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
            PathRoutePredicateFactory.Config config = new PathRoutePredicateFactory.Config();
            config.setPatterns(Collections.singletonList(""));
            Route<HttpServerRequest, HttpServerResponse> test = Route
                    .<HttpServerRequest, HttpServerResponse>builder()
                    .predicate(new PathRoutePredicateFactory().apply(config)).uri("http://www.baidu.com")
                    .id("test")
                    .build();
            routes.add(test);
            return routes;
        }));
    }
}
