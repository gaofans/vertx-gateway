package com.gaofans.vertx.gateway.filter.factory;

import com.gaofans.vertx.gateway.filter.GatewayFilter;


/**
 * 过滤器工厂接口
 * @author Spencer Gibb
 */
@FunctionalInterface
public interface GatewayFilterFactory<C,T,R> {

	/**
	 * 生产过滤器
	 * @param config
	 * @return
	 */
	GatewayFilter<T,R> apply(C config);

}
