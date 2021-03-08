/*
 * Copyright 2013-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gaofans.vertx.gateway.filter.factory;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.support.Configurable;
import com.gaofans.vertx.gateway.support.HasRouteId;
import com.gaofans.vertx.gateway.support.NameUtils;
import com.gaofans.vertx.gateway.support.ShortcutConfigurable;

import java.util.function.Consumer;

/**
 * @author Spencer Gibb
 */
@FunctionalInterface
public interface GatewayFilterFactory<C,T,R> extends ShortcutConfigurable, Configurable<C> {

	/**
	 * Name key.
	 */
	String NAME_KEY = "name";

	/**
	 * Value key.
	 */
	String VALUE_KEY = "value";

	// useful for javadsl
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

	GatewayFilter<T,R> apply(C config);

	default GatewayFilter<T,R> apply(String routeId, C config) {
		if (config instanceof HasRouteId) {
			HasRouteId hasRouteId = (HasRouteId) config;
			hasRouteId.setRouteId(routeId);
		}
		return apply(config);
	}

	default String name() {
		// TODO: deal with proxys
		return NameUtils.normalizeFilterFactoryName(getClass());
	}

}
