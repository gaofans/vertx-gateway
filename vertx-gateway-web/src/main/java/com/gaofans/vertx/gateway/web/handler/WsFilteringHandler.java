package com.gaofans.vertx.gateway.web.handler;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.AbstractFilteringHandler;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

public class WsFilteringHandler extends AbstractFilteringHandler<ServerWebSocket,ServerWebSocket> {

    public WsFilteringHandler(List<GlobalFilter<ServerWebSocket, ServerWebSocket>> globalFilters) {
        super(globalFilters);
    }

    @Override
    public void handle(Exchanger<ServerWebSocket, ServerWebSocket> exchanger) {
        Route<ServerWebSocket, ServerWebSocket> route = exchanger.getRoute();
        if(route == null){
            exchanger.getResponse().end(Buffer.buffer("未找到对应的路由"));
            exchanger.getResponse().reject(404);
        }
        List<GatewayFilter<ServerWebSocket, ServerWebSocket>> gatewayFilters = route.getFilters();

        List<GatewayFilter<ServerWebSocket, ServerWebSocket>> combined = new ArrayList<>(getGlobalFilters());
        combined.addAll(gatewayFilters);
        AnnotationAwareOrderComparator.sort(combined);
        new DefaultGatewayFilterChain<>(combined).filter(exchanger);
    }
}
