package com.gaofans.vertx.gateway.filter;


import io.vertx.core.MultiMap;

import java.util.List;

/**
 * 请求头过滤器接口
 * @author gaofans
 */
public interface HeadersFilter {

	static MultiMap filterRequest(List<HeadersFilter> filters, MultiMap headers) {
		return filter(filters, headers, Type.REQUEST);
	}

	static MultiMap filter(List<HeadersFilter> filters, MultiMap input, Type type) {
		if (filters != null) {
			return filters
						.stream()
						.filter(headersFilter -> headersFilter.supports(type))
						.reduce(input,
								(headers, filter) -> filter.filter(headers),
								(httpHeaders, httpHeaders2) -> {
									httpHeaders.addAll(httpHeaders2);
									return httpHeaders;
						});
		}

		return input;
	}

	/**
	 * 对headers进行过滤
	 * @param input 输入的headers
	 * @return 过滤后的headers
	 */
	MultiMap filter(MultiMap input);

	default boolean supports(Type type) {
		return type.equals(Type.REQUEST);
	}

	enum Type {
		REQUEST, RESPONSE
	}

}
