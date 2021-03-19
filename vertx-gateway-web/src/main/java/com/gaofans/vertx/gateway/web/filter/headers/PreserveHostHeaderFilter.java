package com.gaofans.vertx.gateway.web.filter.headers;

import com.gaofans.vertx.gateway.filter.HeadersFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.web.util.WebUtil;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.headers.HeadersMultiMap;

/**
 * 判断是否去掉host header的过滤器
 * @author gaofans
 */
public class PreserveHostHeaderFilter implements HeadersFilter<HttpServerRequest, HttpServerResponse> {


    @Override
    public MultiMap filter(Exchanger<HttpServerRequest, HttpServerResponse> exchanger, MultiMap input) {
        if(!exchanger.context(WebUtil.PRESERVE_HOST_HEADER_ATTRIBUTE,false)){
            MultiMap filtered = new HeadersMultiMap();
            input.forEach(entry -> {
                if(!HttpHeaderNames.HOST.toString().equalsIgnoreCase(entry.getKey())){
                    filtered.add(entry.getKey(),entry.getValue());
                }
            });
            return filtered;
        }
        return input;
    }

}
