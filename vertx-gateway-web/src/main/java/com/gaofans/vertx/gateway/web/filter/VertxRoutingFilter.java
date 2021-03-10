package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.streams.Pipe;
import org.springframework.core.Ordered;

public class VertxRoutingFilter implements GlobalFilter<HttpServerRequest, HttpServerResponse>, Ordered {

    private final Vertx vertx;

    public VertxRoutingFilter(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void filter(Exchanger<HttpServerRequest, HttpServerResponse> exchanger,
                       GatewayFilterChain<HttpServerRequest, HttpServerResponse> filterChain) {
        vertx.createHttpClient()
                .request(HttpMethod.GET,exchanger.getRoute().getUri().getPort(),
                        exchanger.getRoute().getUri().getHost(),"")
                .onSuccess(req -> {
                    req.headers().setAll(exchanger.getRequest().headers());
                    req.send(exchanger.getRequest()).onSuccess(response -> {
                        response.headers().forEach(header -> {
                            exchanger.getResponse().putHeader(header.getKey(), header.getValue());
                        });
                        exchanger.getResponse().send(response);
                        exchanger.getResponse().end();
                    });


                }).onFailure(event -> {
                    exchanger.getResponse().end(event.getMessage());
                });

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
