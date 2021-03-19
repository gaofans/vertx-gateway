package com.gaofans.vertx.gateway.web.util;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerResponse;

/**
 * 一些常用的方法
 */
public final class WebUtil {

    public static final String PRESERVE_HOST_HEADER_ATTRIBUTE = qualify("preserveHostHeader");

    private WebUtil() {}

    public static void setBadStatus(HttpServerResponse response,Throwable throwable){
        response
                .setStatusCode(HttpResponseStatus.BAD_GATEWAY.code())
                .setStatusMessage(HttpResponseStatus.BAD_GATEWAY.reasonPhrase())
                .end(throwable.getMessage());
    }

    public static void setBadStatus(HttpServerResponse response){
        response
                .setStatusCode(HttpResponseStatus.BAD_GATEWAY.code())
                .setStatusMessage(HttpResponseStatus.BAD_GATEWAY.reasonPhrase())
                .end();
    }

    private static String qualify(String attr) {
        return WebUtil.class.getName() + "." + attr;
    }
}
