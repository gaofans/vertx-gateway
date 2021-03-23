package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.filter.HeadersFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.web.filter.headers.PreserveHostHeaderFilter;
import com.gaofans.vertx.gateway.web.util.WebUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * websocket转发过滤器
 * @author gaofans
 */
public class WsRoutingFilter implements GlobalFilter<HttpServerRequest, HttpServerResponse>, Ordered {

    private final HttpClient httpClient;

    private List<HeadersFilter<HttpServerRequest, HttpServerResponse>> headersFilters;

    public WsRoutingFilter(HttpClient httpClient,
                           List<HeadersFilter<HttpServerRequest, HttpServerResponse>> headersFilters) {
        this.httpClient = httpClient;
        this.headersFilters = headersFilters;
        this.headersFilters = getHeadersFilters();
    }

    public WsRoutingFilter(HttpClient httpClient) {
        this(httpClient,null);
    }

    @Override
    public Future<Void> filter(Exchanger<HttpServerRequest, HttpServerResponse> exchanger,
                         GatewayFilterChain<HttpServerRequest, HttpServerResponse> filterChain) {
        if(exchanger.isRouted()){
            filterChain.filter(exchanger);
        }
        Promise<Void> promise = Promise.promise();
        HttpServerRequest request = exchanger.getRequest();
        HttpServerResponse response = exchanger.getResponse();
        Route<HttpServerRequest, HttpServerResponse> route = exchanger.getRoute();
        if(determine(request)){
            request.toWebSocket().onSuccess(webSocket -> {
                WebSocketConnectOptions options = getWebSocketConnectOptions(exchanger, request, route);
                webSocket.pause();
                httpClient
                        .webSocket(options)
                        .onSuccess(respSocket -> {
                            exchanger.setRouted(true);
                            respSocket
                                    .pipeTo(webSocket)
                                    .onSuccess(event -> {
                                        webSocket.pipeTo(respSocket).onComplete(promise);
                                    }).onFailure(promise::fail);
                        }).onFailure(throwable -> WebUtil.setBadStatus(response,throwable).onComplete(promise));
            }).onFailure(throwable -> WebUtil.setBadStatus(response,throwable).onComplete(promise));
        }else{
            filterChain.filter(exchanger);
        }
        return promise.future();

    }

    /**
     * 判断是否是websocket请求
     */
    private boolean determine(HttpServerRequest request){
        String connection = request.getHeader(HttpHeaderNames.CONNECTION);
        String upgrade = request.getHeader(HttpHeaderNames.UPGRADE);
        return HttpHeaderValues.UPGRADE.toLowerCase().toString().equals(connection.toLowerCase())
                && HttpHeaderValues.WEBSOCKET.toLowerCase().toString().equals(upgrade.toLowerCase());
    }

    private WebSocketConnectOptions getWebSocketConnectOptions(Exchanger<HttpServerRequest, HttpServerResponse> exchanger,
                                                               HttpServerRequest request, Route<HttpServerRequest, HttpServerResponse> route) {
        int port = route.getUri().getPort();
        WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions();
        webSocketConnectOptions.setHeaders(HeadersFilter.filterRequest(exchanger,this.headersFilters, request.headers()));
        webSocketConnectOptions.setMethod(request.method());
        webSocketConnectOptions.setHost(request.host());
        webSocketConnectOptions.setAbsoluteURI(request.absoluteURI());
        webSocketConnectOptions.setPort(port);
        webSocketConnectOptions.setServer(request.localAddress());
        webSocketConnectOptions.setURI(request.uri());
        return webSocketConnectOptions;
    }

    /**
     * 获取请求头过滤器
     * @return 请求头过滤器
     */
    private List<HeadersFilter<HttpServerRequest, HttpServerResponse>> getHeadersFilters(){
        if (this.headersFilters == null){
            this.headersFilters = new ArrayList<>();
        }
        //添加一个过滤器，过滤掉sec-websocket开头的请求头
        headersFilters.add((exchanger,input) -> {
            MultiMap filtered = new HeadersMultiMap();
            input.forEach(entry -> {
                if(!entry.getKey().toLowerCase().startsWith("sec-websocket")){
                    filtered.add(entry.getKey(),entry.getValue());
                }
            });
            return filtered;
        });
        this.headersFilters.add(new PreserveHostHeaderFilter());
        return this.headersFilters;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 1;
    }
}
