package com.gaofans.vertx.gateway.support;

import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.filter.factory.GatewayFilterFactory;
import com.gaofans.vertx.gateway.handler.predicate.RoutePredicateFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Spencer Gibb
 */
public final class NameUtils {

	private NameUtils() {
		throw new AssertionError("Must not instantiate utility class.");
	}

	/**
	 * Generated name prefix.
	 */
	public static final String GENERATED_NAME_PREFIX = "_genkey_";

	private static final Pattern NAME_PATTERN = Pattern.compile("([A-Z][a-z0-9]+)");

	public static String generateName(int i) {
		return GENERATED_NAME_PREFIX + i;
	}

	public static String normalizeRoutePredicateName(Class<? extends RoutePredicateFactory> clazz) {
		return removeGarbage(clazz.getSimpleName().replace(RoutePredicateFactory.class.getSimpleName(), ""));
	}

	public static String normalizeRoutePredicateNameAsProperty(Class<? extends RoutePredicateFactory> clazz) {
		return normalizeToCanonicalPropertyFormat(normalizeRoutePredicateName(clazz));
	}

	public static String normalizeFilterFactoryName(Class<? extends GatewayFilterFactory> clazz) {
		return removeGarbage(clazz.getSimpleName().replace(GatewayFilterFactory.class.getSimpleName(), ""));
	}

	public static String normalizeGlobalFilterName(Class<? extends GlobalFilter> clazz) {
		return removeGarbage(clazz.getSimpleName().replace(GlobalFilter.class.getSimpleName(), "")).replace("Filter",
				"");
	}

	public static String normalizeFilterFactoryNameAsProperty(Class<? extends GatewayFilterFactory> clazz) {
		return normalizeToCanonicalPropertyFormat(normalizeFilterFactoryName(clazz));
	}

	public static String normalizeGlobalFilterNameAsProperty(Class<? extends GlobalFilter> filterClass) {
		return normalizeToCanonicalPropertyFormat(normalizeGlobalFilterName(filterClass));
	}

	public static String normalizeToCanonicalPropertyFormat(String name) {
		Matcher matcher = NAME_PATTERN.matcher(name);
		StringBuffer stringBuffer = new StringBuffer();
		while (matcher.find()) {
			if (stringBuffer.length() != 0) {
				matcher.appendReplacement(stringBuffer, "-" + matcher.group(1).toLowerCase());
			}
			else {
				matcher.appendReplacement(stringBuffer, matcher.group(1).toLowerCase());
			}
		}
		return stringBuffer.toString();
	}

	private static String removeGarbage(String s) {
		int garbageIdx = s.indexOf("$Mockito");
		if (garbageIdx > 0) {
			return s.substring(0, garbageIdx);
		}

		return s;
	}

}
