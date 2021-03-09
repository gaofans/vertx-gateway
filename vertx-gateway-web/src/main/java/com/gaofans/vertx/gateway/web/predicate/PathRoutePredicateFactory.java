package com.gaofans.vertx.gateway.web.predicate;

import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.handler.predicate.AbstractRoutePredicateFactory;
import com.gaofans.vertx.gateway.handler.predicate.GatewayPredicate;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.style.ToStringCreator;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Spencer Gibb
 * @author Dhawal Kapil
 */
public class PathRoutePredicateFactory extends AbstractRoutePredicateFactory<PathRoutePredicateFactory.Config, HttpServerRequest, HttpServerResponse> {

	private static final Log log = LogFactory.getLog(PathRoutePredicateFactory.class);

	private static final String MATCH_TRAILING_SLASH = "matchTrailingSlash";

	public PathRoutePredicateFactory() {
		super(Config.class);
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList("patterns", MATCH_TRAILING_SLASH);
	}

	@Override
	public ShortcutType shortcutType() {
		return ShortcutType.GATHER_LIST_TAIL_FLAG;
	}

	@Override
	public Predicate<Exchanger<HttpServerRequest,HttpServerResponse>> apply(Config config) {

		return (GatewayPredicate<HttpServerRequest, HttpServerResponse>) exchanger -> true;
	}

	@Validated
	public static class Config {

		private List<String> patterns = new ArrayList<>();

		private boolean matchTrailingSlash = true;

		public List<String> getPatterns() {
			return patterns;
		}

		public Config setPatterns(List<String> patterns) {
			this.patterns = patterns;
			return this;
		}

		/**
		 * @deprecated use {@link #isMatchTrailingSlash()}
		 */
		@Deprecated
		public boolean isMatchOptionalTrailingSeparator() {
			return isMatchTrailingSlash();
		}

		/**
		 * @deprecated use {@link #setMatchTrailingSlash(boolean)}
		 */
		@Deprecated
		public Config setMatchOptionalTrailingSeparator(boolean matchOptionalTrailingSeparator) {
			setMatchTrailingSlash(matchOptionalTrailingSeparator);
			return this;
		}

		public boolean isMatchTrailingSlash() {
			return matchTrailingSlash;
		}

		public Config setMatchTrailingSlash(boolean matchTrailingSlash) {
			this.matchTrailingSlash = matchTrailingSlash;
			return this;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("patterns", patterns)
					.append(MATCH_TRAILING_SLASH, matchTrailingSlash).toString();
		}

	}

}
