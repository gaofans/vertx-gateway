package com.gaofans.vertx.gateway.filter;

import com.gaofans.vertx.gateway.handler.Exchanger;
import io.vertx.core.Future;

/**
 * 过滤器链
 * @author GaoFans
 * @since 2021/2/26
 */
public interface GatewayFilterChain<T,R> {

    /**
     * 执行下一个过滤器
     * @param exchanger
     */
    Future<Void> filter(Exchanger<T,R> exchanger);
}
