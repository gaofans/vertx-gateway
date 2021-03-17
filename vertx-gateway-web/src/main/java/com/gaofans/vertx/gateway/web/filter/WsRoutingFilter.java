package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.WebSocketConnectOptions;
import org.springframework.core.Ordered;

public class WsRoutingFilter implements GlobalFilter<HttpServerRequest, HttpServerResponse>, Ordered {

    private HttpClient httpClient;

    public WsRoutingFilter(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void filter(Exchanger<HttpServerRequest, HttpServerResponse> exchanger,
                       GatewayFilterChain<HttpServerRequest, HttpServerResponse> filterChain) {
        if(exchanger.isRouted()){
            filterChain.filter(exchanger);
        }
        HttpServerRequest request = exchanger.getRequest();
        HttpServerResponse response = exchanger.getResponse();
        Route<HttpServerRequest, HttpServerResponse> route = exchanger.getRoute();
        String connection = request.getHeader(HttpHeaderNames.CONNECTION);
        String upgrade = request.getHeader(HttpHeaderNames.UPGRADE);
        if(HttpHeaderValues.UPGRADE.toLowerCase().toString().equals(connection.toLowerCase())
                && HttpHeaderValues.WEBSOCKET.toLowerCase().toString().equals(upgrade.toLowerCase())){
            request.toWebSocket().onSuccess(webSocket -> {
                String host = route.getUri().getHost();
                int port = route.getUri().getPort();
                WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions();
                request.headers().forEach(entry -> {
                    if(!entry.getKey().toLowerCase().startsWith("sec-websocket")){
                        webSocketConnectOptions.putHeader(entry.getKey(),entry.getValue());
                    }
                });
                webSocketConnectOptions.setMethod(request.method());
                webSocketConnectOptions.setHost(request.host());
                webSocketConnectOptions.setAbsoluteURI(request.absoluteURI());
                webSocketConnectOptions.setPort(port);
                webSocketConnectOptions.setServer(request.localAddress());
                webSocketConnectOptions.setURI(request.uri());
                webSocket.pause();

                httpClient
                        .webSocket(webSocketConnectOptions)
                        .onSuccess(respSocket -> {
                            exchanger.setRouted(true);
                            respSocket.pipeTo(webSocket);
                            webSocket.pipeTo(respSocket);
                        }).onFailure(throwable -> {
                            response.setStatusCode(502)
                                    .setStatusMessage("bad gateway")
                                    .end(throwable.getMessage());
                        });
            }).onFailure(throwable -> {
                response.setStatusCode(502)
                    .setStatusMessage("bad gateway")
                    .end(throwable.getMessage());
            });
        }else{
            filterChain.filter(exchanger);
        }

    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 1;
    }
}
