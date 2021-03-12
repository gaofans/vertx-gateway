package com.gaofans.vertx.gateway.web.handler;

import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.route.RouteLocator;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class WsExchanger implements Exchanger<ServerWebSocket, ServerWebSocket> {


    private final ServerWebSocket webSocket;
    private boolean isRouted;
    private Route<ServerWebSocket,ServerWebSocket> route;
    private final Map<String,Object> context;
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpExchanger.class);

    public WsExchanger(ServerWebSocket webSocket,
                       RouteLocator<ServerWebSocket, ServerWebSocket> routeLocator) {
        this.webSocket = webSocket;
        this.context = new HashMap<>();
        for (Route<ServerWebSocket, ServerWebSocket> route : routeLocator.getRoutes()) {
            if (route.getPredicate().test(this)) {
                this.route = route;
                break;
            }
        }
        if(this.route == null){
            LOGGER.warn("{}未找到对应的路由",webSocket.uri());
        }
    }

    @Override
    public ServerWebSocket getRequest() {
        return this.webSocket;
    }

    @Override
    public ServerWebSocket getResponse() {
        return this.webSocket;
    }

    @Override
    public boolean isRouted() {
        return this.isRouted;
    }

    @Override
    public void setRouted(boolean routed) {
        this.isRouted = routed;
    }

    @Override
    public Route<ServerWebSocket, ServerWebSocket> getRoute() {
        return this.route;
    }

    @Override
    public Map<String, Object> context() {
        return this.context;
    }
}
