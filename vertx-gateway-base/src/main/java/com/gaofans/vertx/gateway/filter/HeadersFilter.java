package com.gaofans.vertx.gateway.filter;


import com.gaofans.vertx.gateway.handler.Exchanger;
import io.vertx.core.MultiMap;

import java.util.List;

/**
 * 请求头过滤器接口
 * @author gaofans
 */
public interface HeadersFilter<T,R> {

	static <T,R> MultiMap filterRequest(Exchanger<T,R> exchanger, List<HeadersFilter<T,R>> filters, MultiMap headers) {
		return filter(exchanger, filters, headers, Type.REQUEST);
	}

	static <T,R> MultiMap filter(Exchanger<T,R> exchanger, List<HeadersFilter<T,R>> filters, MultiMap input, Type type) {
		if (filters != null) {
			return filters
						.stream()
						.filter(headersFilter -> headersFilter.supports(type))
						.reduce(input,
								(headers, filter) -> filter.filter(exchanger,headers),
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
	MultiMap filter(Exchanger<T,R> exchanger,MultiMap input);

	default boolean supports(Type type) {
		return type.equals(Type.REQUEST);
	}

	enum Type {
		REQUEST, RESPONSE
	}

}
