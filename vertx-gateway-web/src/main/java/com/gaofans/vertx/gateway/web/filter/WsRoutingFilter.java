package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import org.springframework.core.Ordered;

public class WsRoutingFilter implements GlobalFilter<ServerWebSocket, ServerWebSocket>, Ordered {

    private final Vertx vertx;

    public WsRoutingFilter(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void filter(Exchanger<ServerWebSocket, ServerWebSocket> exchanger, GatewayFilterChain<ServerWebSocket, ServerWebSocket> filterChain) {
        if(exchanger.isRouted()){
            filterChain.filter(exchanger);
        }
        ServerWebSocket webSocket = exchanger.getRequest();
        Route<ServerWebSocket, ServerWebSocket> route = exchanger.getRoute();
        webSocket.pause();
        int targetPort = route.getUri().getPort();
        String targetHost = route.getUri().getHost();
        String targetUri = webSocket.uri();
        vertx.createHttpClient()
                .webSocket(targetPort,targetHost,targetUri)
                .onSuccess(req -> {
                    webSocket.pipe().to(req);
                    req.pipe().to(webSocket);
                })
                .onFailure(event -> {

                });
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
