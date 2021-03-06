package com.gaofans.vertx.gateway.support;

public interface Configurable<C> {

	Class<C> getConfigClass();

	C newConfig();

}
