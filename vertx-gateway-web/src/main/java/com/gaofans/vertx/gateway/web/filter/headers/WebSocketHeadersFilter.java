package com.gaofans.vertx.gateway.web.filter.headers;

import com.gaofans.vertx.gateway.filter.HeadersFilter;
import io.vertx.core.MultiMap;
import io.vertx.core.http.impl.headers.HeadersMultiMap;

/**
 * 过滤掉sec-websocket开头的请求头
 * @author gaofans
 */
public class WebSocketHeadersFilter implements HeadersFilter {
    @Override
    public MultiMap filter(MultiMap input) {
        MultiMap filtered = new HeadersMultiMap();
        input.forEach(entry -> {
            if(!entry.getKey().toLowerCase().startsWith("sec-websocket")){
                filtered.add(entry.getKey(),entry.getValue());
            }
        });
        return filtered;
    }
}


