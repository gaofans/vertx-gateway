package com.gaofans.vertx.gateway.web.filter;

import org.springframework.core.Ordered;

/**
 * 内置过滤器的顺序
 * @author gaofans
 */
public interface FilterOrder {

    /**
     * HttpRoutingFilter序号
     */
    int HTTP_ROUTING_FILTER = Ordered.LOWEST_PRECEDENCE;
    /**
     * WsRoutingFilter序号
     */
    int WS_ROUTING_FILTER = HTTP_ROUTING_FILTER - 1;
    /**
     * WriteResponseFilter序号
     */
    int WRITE_RESPONSE_FILTER = 0;
}
