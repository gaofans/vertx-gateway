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
public class HttpHandlerAdapter implements Handler<HttpServerRequest> {

    private final HttpFilteringHandler filteringHandler;

    public HttpHandlerAdapter(HttpFilteringHandler filteringHandler) {
        this.filteringHandler = filteringHandler;
    }

    @Override
    public void handle(HttpServerRequest httpServerRequest) {
        filteringHandler.handle(new HttpExchanger(httpServerRequest,() -> {
            List<Route<HttpServerRequest, HttpServerResponse>> routes = new ArrayList<>();
            PathRoutePredicateFactory.Config config = new PathRoutePredicateFactory.Config();
            config.setPatterns(Collections.singletonList("/training/**"));
            Route<HttpServerRequest, HttpServerResponse> training = Route
                    .<HttpServerRequest, HttpServerResponse>builder()
                    .predicate(new PathRoutePredicateFactory().apply(config))
                    .uri("http://localhost:8082/training/")
                    .id("training")
                    .build();
            PathRoutePredicateFactory.Config config2 = new PathRoutePredicateFactory.Config();
            config2.setPatterns(Collections.singletonList("/training2/**"));
            Route<HttpServerRequest, HttpServerResponse> training2 = Route
                    .<HttpServerRequest, HttpServerResponse>builder()
                    .predicate(new PathRoutePredicateFactory().apply(config2))
                    .uri("http://localhost:8080/training2/")
                    .id("training2")
                    .build();
            routes.add(training);
            routes.add(training2);
            return routes;
        }));
    }
}
