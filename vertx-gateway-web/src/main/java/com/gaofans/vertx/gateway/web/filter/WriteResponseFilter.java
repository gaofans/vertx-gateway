package com.gaofans.vertx.gateway.web.filter;

import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.web.util.WebUtil;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.streams.ReadStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

/**
 * 将代理返回的response流写出
 * @author gaofans
 */
public class WriteResponseFilter implements GlobalFilter<HttpServerRequest, HttpServerResponse>, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteResponseFilter.class);

    @Override
    public Future<Void> filter(Exchanger<HttpServerRequest, HttpServerResponse> exchanger,
                               GatewayFilterChain<HttpServerRequest, HttpServerResponse> filterChain) {
        return filterChain.filter(exchanger).onSuccess(event -> {
            ReadStream<Buffer> readStream = exchanger.context(WebUtil.CLIENT_RESPONSE_STREAM);
            if(readStream == null || exchanger.getResponse().closed()){
                return;
            }
            readStream
                    .pipeTo(exchanger.getResponse())
                    .onComplete(res -> {
                        if(!res.succeeded()){
                            LOGGER.warn("response写入出错",res.cause());
                        }
                        //关闭response
                        exchanger.getResponse().reset();
                    });
        }).onFailure(event -> {
            WebUtil.setBadStatus(exchanger.getResponse(),event);
        });
    }

    @Override
    public int getOrder() {
        return FilterOrder.WRITE_RESPONSE_FILTER;
    }
}
