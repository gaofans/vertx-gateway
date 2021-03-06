package com.gaofans.vertx.gateway.web;

import com.gaofans.vertx.gateway.filter.OrderedGatewayFilter;
import com.gaofans.vertx.gateway.filter.factory.AbstractGatewayFilterFactory;
import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.route.RouteLocator;
import com.gaofans.vertx.gateway.web.filter.factory.PreserveHostHeaderGatewayFilterFactory;
import com.gaofans.vertx.gateway.web.filter.factory.RemoveRequestHeaderGatewayFilterFactory;
import com.gaofans.vertx.gateway.web.filter.factory.RemoveResponseHeaderGatewayFilterFactory;
import com.gaofans.vertx.gateway.web.filter.factory.RewritePathGatewayFilterFactory;
import com.gaofans.vertx.gateway.web.predicate.PathRoutePredicateFactory;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HttpRouteLocator implements RouteLocator<HttpServerRequest, HttpServerResponse> {

    private static List<Route<HttpServerRequest, HttpServerResponse>> routes;

    static {
        routes = new ArrayList<>();
        PathRoutePredicateFactory.Config config = new PathRoutePredicateFactory.Config();
        config.setPatterns(Collections.singletonList("/training/**"));
        Route<HttpServerRequest, HttpServerResponse> training = Route
                .<HttpServerRequest, HttpServerResponse>builder()
                .predicate(new PathRoutePredicateFactory().apply(config))
                .uri("http://localhost:8082/training/")
                .id("training")
                .filter(new OrderedGatewayFilter<>(new PreserveHostHeaderGatewayFilterFactory().apply(), 0))
                .build();
        PathRoutePredicateFactory.Config config2 = new PathRoutePredicateFactory.Config();
        config2.setPatterns(Collections.singletonList("/training2/**"));
        Route<HttpServerRequest, HttpServerResponse> training2 = Route
                .<HttpServerRequest, HttpServerResponse>builder()
                .predicate(new PathRoutePredicateFactory().apply(config2))
                .uri("http://localhost:8080/training2/")
                .filter(new OrderedGatewayFilter<>(new PreserveHostHeaderGatewayFilterFactory().apply(), 0))
                .id("training2")
                .build();
        PathRoutePredicateFactory.Config config3 = new PathRoutePredicateFactory.Config();
        config3.setPatterns(Collections.singletonList("/baidu/**"));
        RewritePathGatewayFilterFactory.Config rewriteConfig = new RewritePathGatewayFilterFactory.Config();
        rewriteConfig.setReplacement("$\\{segment}");
        rewriteConfig.setRegexp("/baidu(?<segment>.*)");
        AbstractGatewayFilterFactory.NameConfig r = new AbstractGatewayFilterFactory.NameConfig();
        r.setName("User-Agent");
        AbstractGatewayFilterFactory.NameConfig p = new AbstractGatewayFilterFactory.NameConfig();
        p.setName("Location");
        Route<HttpServerRequest, HttpServerResponse> baidu = Route
                .<HttpServerRequest, HttpServerResponse>builder()
                .predicate(new PathRoutePredicateFactory().apply(config3))
                .uri("http://www.baidu.com:80/")
                .filter(new OrderedGatewayFilter<>(new RewritePathGatewayFilterFactory().apply(rewriteConfig),0))
                .filter(new OrderedGatewayFilter<>(new RemoveRequestHeaderGatewayFilterFactory().apply(r),1))
                .filter(new OrderedGatewayFilter<>(new RemoveResponseHeaderGatewayFilterFactory().apply(p),2))
                .id("baidu")
                .build();
        routes.add(training);
        routes.add(training2);
        routes.add(baidu);
    }

    @Override
    public List<Route<HttpServerRequest, HttpServerResponse>> getRoutes() {
        return routes;
    }
}
