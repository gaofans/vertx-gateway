package com.gaofans.vertx.gateway.web.handler;

import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.route.RouteLocator;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * http代理交换器
 */
public class WebExchanger implements Exchanger<HttpServerRequest, HttpServerResponse> {

    private final HttpServerRequest request;
    private final Map<String,Object> context;
    private Route<HttpServerRequest,HttpServerResponse> route;
    private final static Logger LOGGER = LoggerFactory.getLogger(WebExchanger.class);

    public WebExchanger(HttpServerRequest request,
                        RouteLocator<HttpServerRequest,HttpServerResponse> routeLocator) {
        this.request = request;
        this.context = new HashMap<>();
        for (Route<HttpServerRequest, HttpServerResponse> route : routeLocator.getRoutes()) {
            if (route.getPredicate().test(this)) {
                this.route = route;
                break;
            }
        }
        if(this.route == null){
            LOGGER.warn("{}未找到对应的路由",request.absoluteURI());
        }
    }

    @Override
    public HttpServerRequest getRequest() {
        return this.request;
    }

    @Override
    public HttpServerResponse getResponse() {
        return this.request.response();
    }

    @Override
    public Route<HttpServerRequest, HttpServerResponse> getRoute() {
        return this.route;
    }

    @Override
    public Map<String, Object> context() {
        return this.context;
    }
}
