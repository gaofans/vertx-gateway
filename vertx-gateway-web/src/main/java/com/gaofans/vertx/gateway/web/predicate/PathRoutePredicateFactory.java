package com.gaofans.vertx.gateway.web.predicate;

import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.handler.predicate.AbstractRoutePredicateFactory;
import com.gaofans.vertx.gateway.handler.predicate.GatewayPredicate;
import com.gaofans.vertx.gateway.support.UriUtil;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PathRoutePredicateFactory extends AbstractRoutePredicateFactory<PathRoutePredicateFactory.Config, HttpServerRequest, HttpServerResponse> {


	public PathRoutePredicateFactory() {
		super(Config.class);
	}

	@Override
	public Predicate<Exchanger<HttpServerRequest,HttpServerResponse>> apply(Config config) {

		return (GatewayPredicate<HttpServerRequest, HttpServerResponse>) exchanger -> {
			String uri = exchanger.getRequest().uri();
			for (String pattern : config.getPatterns()) {
				if(UriUtil.match(pattern,uri)){
					return true;
				}
			}
			return false;
		};
	}

	@Validated
	public static class Config {

		private List<String> patterns = new ArrayList<>();

		public List<String> getPatterns() {
			return patterns;
		}

		public Config setPatterns(List<String> patterns) {
			this.patterns = patterns;
			return this;
		}

	}

}
