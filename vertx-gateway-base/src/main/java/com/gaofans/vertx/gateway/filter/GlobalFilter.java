package com.gaofans.vertx.gateway.filter;

import com.gaofans.vertx.gateway.handler.Exchanger;
import io.vertx.core.Future;

/**
 * 全局生效的拦截器
 * @author GaoFans
 * @since 2021/2/26
 */
public interface GlobalFilter<T,R> {

    /**
     * 执行过滤器逻辑
     * @param exchanger
     * @param filterChain
     */
    Future<Void> filter(Exchanger<T,R> exchanger, GatewayFilterChain<T,R> filterChain);

}
