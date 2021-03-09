package com.gaofans.vertx.gateway.filter;

import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.support.ShortcutConfigurable;

/**
 * 路由独有的过滤器
 * @author GaoFans
 * @since 2021/2/26
 */
public interface GatewayFilter<T,R> extends ShortcutConfigurable {


    /**
     * 执行过滤器逻辑
     * @param exchanger
     * @param filterChain
     */
    void filter(Exchanger<T,R> exchanger, GatewayFilterChain<T,R> filterChain);
}
