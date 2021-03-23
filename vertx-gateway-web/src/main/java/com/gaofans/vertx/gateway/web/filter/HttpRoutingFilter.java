package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.filter.HeadersFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.web.filter.headers.PreserveHostHeaderFilter;
import com.gaofans.vertx.gateway.web.util.WebUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.Pipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HttpRoutingFilter implements GlobalFilter<HttpServerRequest, HttpServerResponse>, Ordered {

    private final HttpClient httpClient;

    private List<HeadersFilter<HttpServerRequest, HttpServerResponse>> headersFilters;

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpRoutingFilter.class);

    public HttpRoutingFilter(HttpClient httpClient,
                             List<HeadersFilter<HttpServerRequest, HttpServerResponse>> headersFilters) {
        this.httpClient = httpClient;
        this.headersFilters = Objects.requireNonNull(headersFilters);
        this.headersFilters = getHeadersFilter();
    }

    public HttpRoutingFilter(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.headersFilters = getHeadersFilter();
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
        response.exceptionHandler(event -> {
            LOGGER.warn("url:{},route:{},出错:{}",request.uri(),route.getUri().toString(),event.getMessage());
        });
        request.pause();
        HttpMethod targetMethod = request.method();
        int targetPort = route.getUri().getPort();
        String targetHost = route.getUri().getHost();
        String targetUri = Optional.<String>ofNullable(exchanger.context(WebUtil.GATEWAY_REQUEST_URL_ATTR)).orElse(request.uri());
        httpClient
                .request(targetMethod,
                        targetPort,
                        targetHost,
                        targetUri)
                .onSuccess(req -> {
                    req.headers().setAll(HeadersFilter.filterRequest(exchanger,this.headersFilters,request.headers()));
                    Pipe<Buffer> pipe = request.pipe();
                    pipe.endOnSuccess(true);
                    pipe.to(req)
                            .onSuccess(unused -> {
                                req.send()
                                        .onSuccess(targetResponse -> {
                                            if(response.closed()){
                                                return;
                                            }
                                            response.headers().setAll(targetResponse.headers());
                                            response.setStatusCode(targetResponse.statusCode());
                                            Pipe<Buffer> rp = targetResponse.pipe();
                                            rp.to(response);
                                            exchanger.setRouted(true);
                                            filterChain.filter(exchanger);
                                        })
                                        .onFailure(event -> WebUtil.setBadStatus(response,event));
                            })
                            .onFailure(event -> WebUtil.setBadStatus(response,event));
                })
                .onFailure(event -> WebUtil.setBadStatus(response,event));
    }

    /**
     * 添加host header过滤器
     */
    private List<HeadersFilter<HttpServerRequest, HttpServerResponse>> getHeadersFilter(){
        if(this.headersFilters == null){
            this.headersFilters = new ArrayList<>();
        }
        this.headersFilters.add(new PreserveHostHeaderFilter());
        return this.headersFilters;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
