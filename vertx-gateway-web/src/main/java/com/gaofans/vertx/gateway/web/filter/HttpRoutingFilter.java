package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.streams.Pipe;
import org.springframework.core.Ordered;

public class HttpRoutingFilter implements GlobalFilter<HttpServerRequest, HttpServerResponse>, Ordered {

    private final HttpClient httpClient;

    public HttpRoutingFilter(HttpClient httpClient) {
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
        request.pause();
        HttpMethod targetMethod = request.method();
        int targetPort = route.getUri().getPort();
        String targetHost = route.getUri().getHost();
        String targetUri = request.uri();
        httpClient
                .request(targetMethod,
                        targetPort,
                        targetHost,
                        targetUri)
                .onSuccess(req -> {
                    req.headers().setAll(request.headers());
                    Pipe<Buffer> pipe = request.pipe();
                    pipe.endOnSuccess(true);
                    pipe.to(req)
                        .onSuccess(unused -> {
                            req.send().onSuccess(targetResponse -> {
                                response.headers().setAll(targetResponse.headers());
                                response.setStatusCode(targetResponse.statusCode());
                                Pipe<Buffer> rp = targetResponse.pipe();
                                rp.to(response);
                                exchanger.setRouted(true);
                                filterChain.filter(exchanger);
                            });
                        }).onFailure(Throwable::printStackTrace);
                })
                .onFailure(event -> {
                    response.end(event.getMessage());
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
