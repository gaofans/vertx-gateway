package com.gaofans.vertx.gateway.route;

import com.gaofans.vertx.gateway.filter.FilterDefinition;
import com.gaofans.vertx.gateway.handler.predicate.PredicateDefinition;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.*;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * @author Spencer Gibb
 */
@Validated
public class RouteDefinition {

	private String id;

	@NotEmpty
	@Valid
	private List<PredicateDefinition> predicates = new ArrayList<>();

	@Valid
	private List<FilterDefinition> filters = new ArrayList<>();

	@NotNull
	private URI uri;

	private Map<String, Object> metadata = new HashMap<>();

	private int order = 0;

	public RouteDefinition() {
	}

	public RouteDefinition(String text) {
		int eqIdx = text.indexOf('=');
		if (eqIdx <= 0) {
			throw new ValidationException(
					"Unable to parse RouteDefinition text '" + text + "'" + ", must be of the form name=value");
		}

		setId(text.substring(0, eqIdx));

		String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");

		setUri(URI.create(args[0]));

		for (int i = 1; i < args.length; i++) {
			this.predicates.add(new PredicateDefinition(args[i]));
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<PredicateDefinition> getPredicates() {
		return predicates;
	}

	public void setPredicates(List<PredicateDefinition> predicates) {
		this.predicates = predicates;
	}

	public List<FilterDefinition> getFilters() {
		return filters;
	}

	public void setFilters(List<FilterDefinition> filters) {
		this.filters = filters;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RouteDefinition that = (RouteDefinition) o;
		return this.order == that.order && Objects.equals(this.id, that.id)
				&& Objects.equals(this.predicates, that.predicates) && Objects.equals(this.filters, that.filters)
				&& Objects.equals(this.uri, that.uri) && Objects.equals(this.metadata, that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.predicates, this.filters, this.uri, this.metadata, this.order);
	}

	@Override
	public String toString() {
		return "RouteDefinition{" + "id='" + id + '\'' + ", predicates=" + predicates + ", filters=" + filters
				+ ", uri=" + uri + ", order=" + order + ", metadata=" + metadata + '}';
	}

}
