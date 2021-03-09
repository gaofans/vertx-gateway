package com.gaofans.vertx.gateway.handler.predicate;


import com.gaofans.vertx.gateway.support.AbstractConfigurable;

public abstract class AbstractRoutePredicateFactory<C,T,R> extends AbstractConfigurable<C>
		implements RoutePredicateFactory<C,T,R> {

	public AbstractRoutePredicateFactory(Class<C> configClass) {
		super(configClass);
	}

}
