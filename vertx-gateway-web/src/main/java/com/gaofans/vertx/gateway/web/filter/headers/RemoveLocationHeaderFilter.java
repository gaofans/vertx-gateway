package com.gaofans.vertx.gateway.web.filter.headers;

import com.gaofans.vertx.gateway.filter.HeadersFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.headers.HeadersMultiMap;

public class RemoveLocationHeaderFilter implements HeadersFilter<HttpServerRequest, HttpServerResponse> {
    @Override
    public MultiMap filter(Exchanger<HttpServerRequest, HttpServerResponse> exchanger, MultiMap input) {
        MultiMap filtered = new HeadersMultiMap();
        input.forEach(entry -> {
            if(!HttpHeaderNames.LOCATION.toString().equalsIgnoreCase(entry.getKey())){
                filtered.add(entry.getKey(),entry.getValue());
            }
        });
        return filtered;
    }

    @Override
    public boolean supports(Type type) {
        return type.equals(Type.RESPONSE);
    }
}
