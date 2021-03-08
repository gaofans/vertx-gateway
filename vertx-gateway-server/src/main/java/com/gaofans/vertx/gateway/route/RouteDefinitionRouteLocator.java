package com.gaofans.vertx.gateway.route;

import com.gaofans.vertx.gateway.config.GatewayProperties;
import com.gaofans.vertx.gateway.filter.FilterDefinition;
import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.OrderedGatewayFilter;
import com.gaofans.vertx.gateway.filter.factory.GatewayFilterFactory;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.handler.predicate.GatewayPredicate;
import com.gaofans.vertx.gateway.handler.predicate.PredicateDefinition;
import com.gaofans.vertx.gateway.handler.predicate.RoutePredicateFactory;
import com.gaofans.vertx.gateway.support.ConfigurationService;
import com.gaofans.vertx.gateway.support.HasRouteId;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * @author Spencer Gibb
 */
public class RouteDefinitionRouteLocator<T,R> implements RouteLocator<T,R> {

	/**
	 * Default filters name.
	 */
	public static final String DEFAULT_FILTERS = "defaultFilters";

	protected final Log logger = LogFactory.getLog(getClass());

	private final RouteDefinitionLocator routeDefinitionLocator;

	private final ConfigurationService configurationService;

	private final Map<String, RoutePredicateFactory<Object,T,R>> predicates = new LinkedHashMap<>();

	private final Map<String, GatewayFilterFactory<Object,T,R>> gatewayFilterFactories = new HashMap<>();

	private final GatewayProperties gatewayProperties;

	public RouteDefinitionRouteLocator(RouteDefinitionLocator routeDefinitionLocator,
                                       List<RoutePredicateFactory<Object,T,R>> predicates,
									   List<GatewayFilterFactory<Object,T,R>> gatewayFilterFactories,
                                       GatewayProperties gatewayProperties,
									   ConfigurationService configurationService) {
		this.routeDefinitionLocator = routeDefinitionLocator;
		initFactories(predicates);
		gatewayFilterFactories.forEach(factory -> this.gatewayFilterFactories.put(factory.name(), factory));
		this.gatewayProperties = gatewayProperties;
		this.configurationService = configurationService;
	}

	private void initFactories(List<RoutePredicateFactory<Object,T,R>> predicates) {
		predicates.forEach(factory -> {
			String key = factory.name();
			if (this.predicates.containsKey(key)) {
				this.logger.warn("A RoutePredicateFactory named " + key + " already exists, class: "
						+ this.predicates.get(key) + ". It will be overwritten.");
			}
			this.predicates.put(key, factory);
			if (logger.isInfoEnabled()) {
				logger.info("Loaded RoutePredicateFactory [" + key + "]");
			}
		});
	}

	@Override
	public List<Route<T,R>> getRoutes() {
		return this.routeDefinitionLocator.getRouteDefinitions().stream().map(this::convertToRoute).collect(Collectors.toList());
	}

	private Route<T,R> convertToRoute(RouteDefinition routeDefinition) {
		Predicate<Exchanger<T,R>> predicate = combinePredicates(routeDefinition);
		List<GatewayFilter<T,R>> gatewayFilters = getFilters(routeDefinition);

		return Route.<T,R>builder().predicate(predicate).replaceFilters(gatewayFilters).build();
	}

	@SuppressWarnings("unchecked")
	List<GatewayFilter<T,R>> loadGatewayFilters(String id, List<FilterDefinition> filterDefinitions) {
		ArrayList<GatewayFilter<T,R>> ordered = new ArrayList<>(filterDefinitions.size());
		for (int i = 0; i < filterDefinitions.size(); i++) {
			FilterDefinition definition = filterDefinitions.get(i);
			GatewayFilterFactory<Object,T,R> factory = this.gatewayFilterFactories.get(definition.getName());
			if (factory == null) {
				throw new IllegalArgumentException(
						"Unable to find GatewayFilterFactory with name " + definition.getName());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("RouteDefinition " + id + " applying filter " + definition.getArgs() + " to "
						+ definition.getName());
			}

			// @formatter:off
			Object configuration = this.configurationService.with(factory)
					.name(definition.getName())
					.properties(definition.getArgs())
					.bind();
			// @formatter:on

			// some filters require routeId
			// TODO: is there a better place to apply this?
			if (configuration instanceof HasRouteId) {
				HasRouteId hasRouteId = (HasRouteId) configuration;
				hasRouteId.setRouteId(id);
			}

			GatewayFilter<T,R> gatewayFilter = factory.apply(configuration);
			if (gatewayFilter instanceof Ordered) {
				ordered.add(gatewayFilter);
			}
			else {
				ordered.add(new OrderedGatewayFilter<T,R>(gatewayFilter, i + 1));
			}
		}

		return ordered;
	}

	private List<GatewayFilter<T,R>> getFilters(RouteDefinition routeDefinition) {
		List<GatewayFilter<T,R>> filters = new ArrayList<>();

		// TODO: support option to apply defaults after route specific filters?
		if (!this.gatewayProperties.getDefaultFilters().isEmpty()) {
			filters.addAll(
					loadGatewayFilters(DEFAULT_FILTERS, new ArrayList<>(this.gatewayProperties.getDefaultFilters())));
		}

		if (!routeDefinition.getFilters().isEmpty()) {
			filters.addAll(loadGatewayFilters(routeDefinition.getId(), new ArrayList<>(routeDefinition.getFilters())));
		}

		AnnotationAwareOrderComparator.sort(filters);
		return filters;
	}

	private Predicate<Exchanger<T,R>> combinePredicates(RouteDefinition routeDefinition) {
		List<PredicateDefinition> predicates = routeDefinition.getPredicates();
		if (predicates == null || predicates.isEmpty()) {
			// this is a very rare case, but possible, just match all
			return GatewayPredicate.wrapIfNeeded(exchanger -> true);
		}
		Predicate<Exchanger<T,R>> predicate = lookup(routeDefinition, predicates.get(0));

		for (PredicateDefinition andPredicate : predicates.subList(1, predicates.size())) {
			Predicate<Exchanger<T,R>> found = lookup(routeDefinition, andPredicate);
			predicate = predicate.and(found);
		}

		return predicate;
	}

	@SuppressWarnings("unchecked")
	private Predicate<Exchanger<T,R>> lookup(RouteDefinition route, PredicateDefinition predicate) {
		RoutePredicateFactory<Object,T,R> factory = this.predicates.get(predicate.getName());
		if (factory == null) {
			throw new IllegalArgumentException("Unable to find RoutePredicateFactory with name " + predicate.getName());
		}
		if (logger.isDebugEnabled()) {
			logger.debug("RouteDefinition " + route.getId() + " applying " + predicate.getArgs() + " to "
					+ predicate.getName());
		}

		Object config = factory.newConfig();
		return factory.apply(config);
	}

}
