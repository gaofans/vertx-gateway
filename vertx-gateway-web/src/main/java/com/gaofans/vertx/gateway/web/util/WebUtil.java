package com.gaofans.vertx.gateway.web.util;

import com.gaofans.vertx.gateway.handler.Exchanger;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.net.URI;
import java.util.LinkedHashSet;

/**
 * 一些常用的方法
 */
public final class WebUtil {

    public static final String PRESERVE_HOST_HEADER_ATTRIBUTE = qualify("preserveHostHeader");
    public static final String GATEWAY_ORIGINAL_REQUEST_URL_ATTR = qualify("gatewayOriginalRequestUrl");
    public static final String GATEWAY_REQUEST_URL_ATTR = qualify("gatewayRequestUrl");

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

    public static void addOriginalRequestUrl(Exchanger<HttpServerRequest,HttpServerResponse> exchanger, URI url) {
        exchanger.context().computeIfAbsent(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, s -> new LinkedHashSet<>());
        LinkedHashSet<URI> uris = exchanger.requiredContext(GATEWAY_ORIGINAL_REQUEST_URL_ATTR);
        uris.add(url);
    }

    private static String qualify(String attr) {
        return WebUtil.class.getName() + "." + attr;
    }
}
