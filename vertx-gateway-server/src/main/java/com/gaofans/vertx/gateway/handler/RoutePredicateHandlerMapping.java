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

package com.gaofans.vertx.gateway.handler;

import com.gaofans.vertx.gateway.route.Route;
import com.gaofans.vertx.gateway.route.RouteLocator;
import org.springframework.core.env.Environment;


/**
 * @author Spencer Gibb
 */
public abstract class RoutePredicateHandlerMapping<T,R> {

	private final FilteringHandler<T,R> handler;

	private final RouteLocator<T,R> routeLocator;

	private final Integer managementPort;

	private final ManagementPortType managementPortType;

	public RoutePredicateHandlerMapping(FilteringHandler<T,R> handler,
										RouteLocator<T,R> routeLocator,
										Environment environment) {
		this.handler = handler;
		this.routeLocator = routeLocator;

		this.managementPort = getPortProperty(environment, "management.server.");
		this.managementPortType = getManagementPortType(environment);
	}

	private ManagementPortType getManagementPortType(Environment environment) {
		Integer serverPort = getPortProperty(environment, "server.");
		if (this.managementPort != null && this.managementPort < 0) {
			return ManagementPortType.DISABLED;
		}
		return ((this.managementPort == null || (serverPort == null && this.managementPort.equals(8080))
				|| (this.managementPort != 0 && this.managementPort.equals(serverPort))) ? ManagementPortType.SAME : ManagementPortType.DIFFERENT);
	}

	private static Integer getPortProperty(Environment environment, String prefix) {
		return environment.getProperty(prefix + "port", Integer.class);
	}

	protected Route<T,R> lookupRoute(Exchanger<T,R> exchange) {

		for (Route<T, R> route : this.routeLocator.getRoutes()) {
			if(route.getPredicate().test(exchange)){
				return route;
			}
		}

		throw new NullPointerException("找不到对应的路由");

	}

	/**
	 * Validate the given handler against the current request.
	 * <p>
	 * The default implementation is empty. Can be overridden in subclasses, for example
	 * to enforce specific preconditions expressed in URL mappings.
	 * @param route the Route object to validate
	 * @param exchange current exchange
	 * @throws Exception if validation failed
	 */
	@SuppressWarnings("UnusedParameters")
	protected void validateRoute(Route<T,R> route, Exchanger<T,R> exchange) {
	}

	protected String getSimpleName() {
		return "RoutePredicateHandlerMapping";
	}

	public enum ManagementPortType {

		/**
		 * The management port has been disabled.
		 */
		DISABLED,

		/**
		 * The management port is the same as the server port.
		 */
		SAME,

		/**
		 * The management port and server port are different.
		 */
		DIFFERENT;

	}

}
