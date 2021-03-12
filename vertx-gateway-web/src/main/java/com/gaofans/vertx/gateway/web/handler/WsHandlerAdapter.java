package com.gaofans.vertx.gateway.web.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;


public class WsHandlerAdapter implements Handler<ServerWebSocket> {

    private WsFilteringHandler filteringHandler;

    public WsHandlerAdapter(WsFilteringHandler filteringHandler) {
        this.filteringHandler = filteringHandler;
    }

    @Override
    public void handle(ServerWebSocket event) {

    }

}
