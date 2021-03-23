package com.gaofans.vertx.gateway.filter;

import com.gaofans.vertx.gateway.handler.Exchanger;
import io.vertx.core.Future;
import org.springframework.core.Ordered;

/**
 * 有序的过滤器包装类
 * @author Spencer Gibb
 */
public class OrderedGatewayFilter<T,R> implements GatewayFilter<T,R>, Ordered {

	private final GatewayFilter<T,R> delegate;

	private final int order;

	public OrderedGatewayFilter(GatewayFilter<T,R> delegate, int order) {
		this.delegate = delegate;
		this.order = order;
	}

	public GatewayFilter<T,R> getDelegate() {
		return delegate;
	}

	@Override
	public Future<Void> filter(Exchanger<T,R> exchange, GatewayFilterChain<T,R> chain) {
		return this.delegate.filter(exchange, chain);
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@Override
	public String toString() {
		return "[" + delegate + ", order = " + order + "]";
	}

}
