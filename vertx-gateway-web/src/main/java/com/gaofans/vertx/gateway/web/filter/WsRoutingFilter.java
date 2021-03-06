package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.filter.HeadersFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.web.filter.headers.PreserveHostHeaderFilter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static String SEC_WEBSOCKET = "sec-websocket";

    private static final Logger LOGGER = LoggerFactory.getLogger(WsRoutingFilter.class);

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
            return filterChain.filter(exchanger);
        }
        Promise<Void> promise = Promise.promise();
        HttpServerRequest request = exchanger.getRequest();
        Route<HttpServerRequest, HttpServerResponse> route = exchanger.getRoute();
        if(determine(request)){
            request.toWebSocket().onSuccess(webSocket -> {
                WebSocketConnectOptions options = getWebSocketConnectOptions(exchanger, request, route);
                webSocket.pause();
                httpClient
                        .webSocket(options)
                        .onSuccess(respSocket -> {
                            exchanger.setRouted(true);
                            CompositeFuture.all(respSocket.pipeTo(webSocket),webSocket.pipeTo(respSocket)).onComplete(res -> {
                                if(res.failed()){
                                    LOGGER.warn("websocket连接异常,route:{},cause:{}",route.getUri().toString(),res.cause().getMessage());
                                }
                                closeConnection(exchanger,promise,webSocket,respSocket);
                            });
                        })
                        .onFailure(err -> {
                            webSocket.close().onComplete(res -> {
                                LOGGER.warn("websocket关闭失败,route:{},cause:{}", exchanger.getRoute().getUri().toString(),res.cause().getMessage());
                                promise.fail(err);
                            });
                        });
            }).onFailure(promise::fail);
        }else{
            filterChain.filter(exchanger).onComplete(promise);
        }
        return promise.future();

    }

    private void closeConnection(Exchanger<HttpServerRequest, HttpServerResponse> exchanger, Promise<Void> promise, io.vertx.core.http.ServerWebSocket webSocket, io.vertx.core.http.WebSocket respSocket) {

        CompositeFuture.all(respSocket.close(), webSocket.close()).onComplete(res -> {
            if(res.succeeded()){
                LOGGER.info("websocket关闭成功,route:{}", exchanger.getRoute().getUri().toString());
            }else{
                LOGGER.warn("websocket关闭失败,route:{},cause:{}", exchanger.getRoute().getUri().toString(),res.cause().getMessage());
            }
            promise.complete();
        });
    }

    /**
     * 判断是否是websocket请求
     */
    private boolean determine(HttpServerRequest request){
        String connection = request.getHeader(HttpHeaderNames.CONNECTION);
        String upgrade = request.getHeader(HttpHeaderNames.UPGRADE);
        return connection != null
                && upgrade != null
                && HttpHeaderValues.UPGRADE.toLowerCase().toString().equals(connection.toLowerCase())
                && HttpHeaderValues.WEBSOCKET.toLowerCase().toString().equals(upgrade.toLowerCase());
    }

    private WebSocketConnectOptions getWebSocketConnectOptions(Exchanger<HttpServerRequest, HttpServerResponse> exchanger,
                                                               HttpServerRequest request, Route<HttpServerRequest, HttpServerResponse> route) {
        int port = route.getUri().getPort();
        String host = route.getUri().getHost();
        WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions();
        webSocketConnectOptions.setHeaders(HeadersFilter.filterRequest(exchanger,this.headersFilters, request.headers()));
        webSocketConnectOptions.setMethod(request.method());
        webSocketConnectOptions.setHost(host);
        webSocketConnectOptions.setPort(port);
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
                if(!entry.getKey().toLowerCase().startsWith(SEC_WEBSOCKET)){
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
        return FilterOrder.WS_ROUTING_FILTER;
    }
}
