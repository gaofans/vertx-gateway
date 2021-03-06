package com.gaofans.vertx.gateway.handler;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.GatewayFilterChain;
import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.filter.OrderedGatewayFilter;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author GaoFans
 * @since 2021/2/27
 */
public abstract class AbstractFilteringHandler<T,R> implements FilteringHandler<T,R>{

    private final List<GatewayFilter<T,R>> globalFilters;

    public AbstractFilteringHandler(List<GlobalFilter<T, R>> globalFilters) {
        this.globalFilters = loadFilters(globalFilters);
    }

    protected List<GatewayFilter<T, R>> getGlobalFilters() {
        return globalFilters;
    }

    protected static <T,R> List<GatewayFilter<T, R>> loadFilters(List<GlobalFilter<T, R>> filters) {
        return filters.stream().map(filter -> {
            GatewayFilterAdapter<T,R> gatewayFilter = new GatewayFilterAdapter<>(filter);
            if (filter instanceof Ordered) {
                int order = ((Ordered) filter).getOrder();
                return new OrderedGatewayFilter<>(gatewayFilter, order);
            }
            return gatewayFilter;
        }).collect(Collectors.toList());
    }

    protected static class GatewayFilterAdapter<T,R> implements GatewayFilter<T,R> {

        private final GlobalFilter<T,R> delegate;

        GatewayFilterAdapter(GlobalFilter<T,R> delegate) {
            this.delegate = delegate;
        }

        @Override
        public String toString() {
            return "GatewayFilterAdapter{" + "delegate=" + delegate +'}';
        }

        @Override
        public void filter(Exchanger<T, R> exchanger, GatewayFilterChain<T, R> filterChain) {
            this.delegate.filter(exchanger, filterChain);
        }
    }

    protected static class DefaultGatewayFilterChain<T,R> implements GatewayFilterChain<T,R> {

        private final int index;

        private final List<GatewayFilter<T,R>> filters;

        public DefaultGatewayFilterChain(List<GatewayFilter<T, R>> filters) {
            this.filters = filters;
            this.index = 0;
        }

        private DefaultGatewayFilterChain(DefaultGatewayFilterChain<T,R> parent, int index) {
            this.filters = parent.getFilters();
            this.index = index;
        }

        public List<GatewayFilter<T,R>> getFilters() {
            return filters;
        }

        @Override
        public void filter(Exchanger<T,R> exchanger) {
            if (this.index < filters.size()) {
                GatewayFilter<T,R> filter = filters.get(this.index);
                DefaultGatewayFilterChain<T,R> chain = new DefaultGatewayFilterChain<>(this, this.index + 1);
                filter.filter(exchanger, chain);
            }
        }

    }
}
