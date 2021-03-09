package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.springframework.core.Ordered;

public class VertxRoutingFilter implements GlobalFilter<HttpServerRequest, HttpServerResponse>, Ordered {

    private final Vertx vertx;

    public VertxRoutingFilter(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void filter(Exchanger<HttpServerRequest, HttpServerResponse> exchanger,
                       GatewayFilterChain<HttpServerRequest, HttpServerResponse> filterChain) {
        Future<HttpClientRequest> request = vertx.createHttpClient()
                .request(HttpMethod.GET,exchanger.getRoute().getUri().getPort(),exchanger.getRoute().getUri().getHost(),"");
        request.onComplete(event -> {
            if(event.succeeded()){
                event.result().send().onComplete(event1 -> {
                    if(event1.succeeded()){
                        event1.result().body().onComplete(event2 -> {
                            exchanger.getResponse().putHeader("Content-Length",event2.result().length()+"");
                            exchanger.getResponse().write(event2.result());
                            filterChain.filter(exchanger);
                            exchanger.getResponse().end();
                        });
                    }
                });
            }
        }).onFailure(event -> {
            exchanger.getResponse().end(event.getMessage());
        });

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
