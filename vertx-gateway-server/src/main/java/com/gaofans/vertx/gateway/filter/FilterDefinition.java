package com.gaofans.vertx.gateway.filter;

import com.gaofans.vertx.gateway.support.NameUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.util.StringUtils.tokenizeToStringArray;

/**
 * @author Spencer Gibb
 */
@Validated
public class FilterDefinition {

	@NotNull
	private String name;

	private Map<String, String> args = new LinkedHashMap<>();

	public FilterDefinition() {
	}

	public FilterDefinition(String text) {
		int eqIdx = text.indexOf('=');
		if (eqIdx <= 0) {
			setName(text);
			return;
		}
		setName(text.substring(0, eqIdx));

		String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");

		for (int i = 0; i < args.length; i++) {
			this.args.put(NameUtils.generateName(i), args[i]);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getArgs() {
		return args;
	}

	public void setArgs(Map<String, String> args) {
		this.args = args;
	}

	public void addArg(String key, String value) {
		this.args.put(key, value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		FilterDefinition that = (FilterDefinition) o;
		return Objects.equals(name, that.name) && Objects.equals(args, that.args);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, args);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("FilterDefinition{");
		sb.append("name='").append(name).append('\'');
		sb.append(", args=").append(args);
		sb.append('}');
		return sb.toString();
	}

}
