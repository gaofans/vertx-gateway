package com.gaofans.vertx.gateway.route;

import java.util.List;

/**
 * @author Spencer Gibb
 */
public interface RouteLocator<T,R> {

	List<Route<T,R>> getRoutes();

}
