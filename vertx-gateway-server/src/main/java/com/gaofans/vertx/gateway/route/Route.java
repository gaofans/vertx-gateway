package com.gaofans.vertx.gateway.route;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.handler.Exchanger;
import com.gaofans.vertx.gateway.handler.predicate.GatewayPredicate;
import com.gaofans.vertx.gateway.route.builder.Buildable;
import org.springframework.core.Ordered;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author GaoFans
 * @since 2021/2/27
 */
public class Route<T,R> implements Ordered {

    private final String id;
    private final URI uri;
    private final int order;
    private final GatewayPredicate<T,R> predicate;
    private final List<GatewayFilter<T,R>> gatewayFilters;
    private final Map<String, Object> metadata;

    public Route(String id,
                 URI uri,
                 int order,
                 GatewayPredicate<T, R> predicate,
                 List<GatewayFilter<T,R>> gatewayFilters,
                 Map<String, Object> metadata) {
        this.id = id;
        this.uri = uri;
        this.order = order;
        this.predicate = predicate;
        this.gatewayFilters = gatewayFilters;
        this.metadata = metadata;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    public String getId() {
        return id;
    }

    public URI getUri() {
        return uri;
    }

    public GatewayPredicate<T, R> getPredicate() {
        return predicate;
    }

    public List<GatewayFilter<T,R>> getFilters() {
        return Collections.unmodifiableList(this.gatewayFilters);
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static <T,R> Builder<T,R> builder() {
        return new Builder<>();
    }

    public static <T,R> Builder<T,R> builder(RouteDefinition routeDefinition) {
        return new Builder<T,R>().id(routeDefinition.getId())
                .uri(routeDefinition.getUri())
                .order(routeDefinition.getOrder())
                .metadata(routeDefinition.getMetadata());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Route<T,R> route = (Route<T,R>) o;
        return this.order == route.order && Objects.equals(this.id, route.id) && Objects.equals(this.uri, route.uri)
                && Objects.equals(this.predicate, route.predicate)
                && Objects.equals(this.gatewayFilters, route.gatewayFilters)
                && Objects.equals(this.metadata, route.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.uri, this.order, this.predicate, this.gatewayFilters, this.metadata);
    }

    @Override
    public String toString() {
        return "Route{" + "id='" + id + '\'' +
                ", uri=" + uri +
                ", order=" + order +
                ", predicate=" + predicate +
                ", gatewayFilters=" + gatewayFilters +
                ", metadata=" + metadata +
                '}';
    }

    public abstract static class AbstractBuilder<B extends AbstractBuilder<B,T,R>,T,R> implements Buildable<Route<T,R>> {

        protected String id;

        protected URI uri;

        protected int order = 0;

        protected List<GatewayFilter<T,R>> gatewayFilters = new ArrayList<>();

        protected Map<String, Object> metadata = new HashMap<>();

        protected AbstractBuilder() {
        }

        protected abstract B getThis();

        public B id(String id) {
            this.id = id;
            return getThis();
        }

        public String getId() {
            return id;
        }

        public B order(int order) {
            this.order = order;
            return getThis();
        }

        public B uri(String uri) {
            return uri(URI.create(uri));
        }

        public B uri(URI uri) {
            this.uri = uri;
            String scheme = this.uri.getScheme();
            Assert.hasText(scheme, "The parameter [" + this.uri + "] format is incorrect, scheme can not be empty");
            if (this.uri.getPort() < 0 && scheme.startsWith("http")) {
                // default known http ports
                int port = this.uri.getScheme().equals("https") ? 443 : 80;
                try {
                    this.uri = new URI(uri.toString()+":"+port);
                }catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
            return getThis();
        }

        public B replaceMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return getThis();
        }

        public B metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return getThis();
        }

        public B metadata(String key, Object value) {
            this.metadata.put(key, value);
            return getThis();
        }

        public abstract GatewayPredicate<T,R> getPredicate();

        public B replaceFilters(List<GatewayFilter<T,R>> gatewayFilters) {
            this.gatewayFilters = gatewayFilters;
            return getThis();
        }

        public B filter(GatewayFilter<T,R> gatewayFilter) {
            this.gatewayFilters.add(gatewayFilter);
            return getThis();
        }

        public B filters(Collection<GatewayFilter<T,R>> gatewayFilters) {
            this.gatewayFilters.addAll(gatewayFilters);
            return getThis();
        }

        public B filters(GatewayFilter<T,R>... gatewayFilters) {
            return filters(Arrays.asList(gatewayFilters));
        }

        @Override
        public Route<T,R> build() {
            Assert.notNull(this.id, "id can not be null");
            Assert.notNull(this.uri, "uri can not be null");
            GatewayPredicate<T,R> predicate = getPredicate();
            Assert.notNull(predicate, "predicate can not be null");

            return new Route<T,R>(this.id, this.uri, this.order, predicate, this.gatewayFilters, this.metadata);
        }

    }

    public static class Builder<T,R> extends AbstractBuilder<Builder<T,R>,T,R> {

        protected Predicate<Exchanger<T,R>> predicate;

        public Builder<T,R> predicate(Predicate<Exchanger<T,R>> predicate){
            this.predicate = predicate;
            return this;
        }

        @Override
        protected Builder<T,R> getThis() {
            return this;
        }

        @Override
        public GatewayPredicate<T,R> getPredicate() {
            return GatewayPredicate.wrapIfNeeded(this.predicate);
        }

        public Builder<T,R> and(Predicate<Exchanger<T,R>> predicate) {
            Assert.notNull(this.predicate, "can not call and() on null predicate");
            this.predicate = this.predicate.and(predicate);
            return this;
        }

        public Builder<T,R> or(Predicate<Exchanger<T,R>> predicate) {
            Assert.notNull(this.predicate, "can not call or() on null predicate");
            this.predicate = this.predicate.or(predicate);
            return this;
        }

        public Builder<T,R> negate() {
            Assert.notNull(this.predicate, "can not call negate() on null predicate");
            this.predicate = this.predicate.negate();
            return this;
        }

    }
}
