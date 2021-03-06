package com.gaofans.vertx.gateway.handler.predicate;


import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.support.Configurable;
import com.gaofans.vertx.gateway.support.NameUtils;
import com.gaofans.vertx.gateway.support.ShortcutConfigurable;

import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * @author Spencer Gibb
 */
@FunctionalInterface
public interface RoutePredicateFactory<C,T,R> extends ShortcutConfigurable, Configurable<C> {

	/**
	 * Pattern key.
	 */
	String PATTERN_KEY = "pattern";

	default Predicate<Exchanger<T,R>> apply(Consumer<C> consumer) {
		C config = newConfig();
		consumer.accept(config);
		beforeApply(config);
		return apply(config);
	}

	default Class<C> getConfigClass() {
		throw new UnsupportedOperationException("getConfigClass() not implemented");
	}

	@Override
	default C newConfig() {
		throw new UnsupportedOperationException("newConfig() not implemented");
	}

	default void beforeApply(C config) {
	}

	Predicate<Exchanger<T,R>> apply(C config);

	default String name() {
		return NameUtils.normalizeRoutePredicateName(getClass());
	}

}
