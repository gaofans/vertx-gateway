package com.gaofans.vertx.gateway.web.handler;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.AbstractFilteringHandler;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.route.Route;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * http代理处理句柄
 * @author GaoFans
 * @since 2021/2/26
 */
public class WebFilteringHandler extends AbstractFilteringHandler<HttpServerRequest, HttpServerResponse> {

    public WebFilteringHandler(List<GlobalFilter<HttpServerRequest, HttpServerResponse>> globalFilters) {
        super(globalFilters);
    }

    @Override
    public void handle(Exchanger<HttpServerRequest, HttpServerResponse> exchanger) {
        Route<HttpServerRequest, HttpServerResponse> route = exchanger.getRoute();
        if(route == null){
            exchanger.getResponse().setStatusCode(404).end();
            return;
        }
        List<GatewayFilter<HttpServerRequest, HttpServerResponse>> gatewayFilters = route.getFilters();

        List<GatewayFilter<HttpServerRequest, HttpServerResponse>> combined = new ArrayList<>(getGlobalFilters());
        combined.addAll(gatewayFilters);
        AnnotationAwareOrderComparator.sort(combined);
        new DefaultGatewayFilterChain<>(combined).filter(exchanger);
    }
}
