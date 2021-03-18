package com.gaofans.vertx.gateway.filter.factory;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.support.Configurable;
import com.gaofans.vertx.gateway.support.HasRouteId;
import com.gaofans.vertx.gateway.support.NameUtils;
import com.gaofans.vertx.gateway.support.ShortcutConfigurable;

import java.util.function.Consumer;


/**
 * 过滤器工厂接口
 */
public interface GatewayFilterFactory<C,T,R> extends ShortcutConfigurable, Configurable<C> {

	/**
	 * 生产过滤器
	 * @param config
	 * @return
	 */
	GatewayFilter<T,R> apply(C config);

	/**
	 * Name key.
	 */
	String NAME_KEY = "name";

	/**
	 * Value key.
	 */
	String VALUE_KEY = "value";

	default GatewayFilter<T,R> apply(String routeId, Consumer<C> consumer) {
		C config = newConfig();
		consumer.accept(config);
		return apply(routeId, config);
	}

	default GatewayFilter<T,R> apply(Consumer<C> consumer) {
		C config = newConfig();
		consumer.accept(config);
		return apply(config);
	}

	default Class<C> getConfigClass() {
		throw new UnsupportedOperationException("getConfigClass() not implemented");
	}

	@Override
	default C newConfig() {
		throw new UnsupportedOperationException("newConfig() not implemented");
	}

	default GatewayFilter<T,R> apply(String routeId, C config) {
		if (config instanceof HasRouteId) {
			HasRouteId hasRouteId = (HasRouteId) config;
			hasRouteId.setRouteId(routeId);
		}
		return apply(config);
	}

	default String name() {
		return NameUtils.normalizeFilterFactoryName(getClass());
	}

}
