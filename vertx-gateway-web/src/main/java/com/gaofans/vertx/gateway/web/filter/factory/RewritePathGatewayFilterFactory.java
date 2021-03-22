package com.gaofans.vertx.gateway.web.filter.factory;

import com.gaofans.vertx.gateway.filter.GatewayFilter;
import com.gaofans.vertx.gateway.filter.factory.AbstractGatewayFilterFactory;
import com.gaofans.vertx.gateway.web.util.WebUtil;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.springframework.util.Assert;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class RewritePathGatewayFilterFactory extends AbstractGatewayFilterFactory<RewritePathGatewayFilterFactory.Config, HttpServerRequest, HttpServerResponse> {

    /**
     * Regexp key.
     */
    public static final String REGEXP_KEY = "regexp";

    /**
     * Replacement key.
     */
    public static final String REPLACEMENT_KEY = "replacement";

    public RewritePathGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(REGEXP_KEY, REPLACEMENT_KEY);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public GatewayFilter<HttpServerRequest, HttpServerResponse> apply(Config config) {
        String replacement = config.replacement.replace("$\\", "$");
        return (exchanger, filterChain) -> {
            try{
                HttpServerRequest request = exchanger.getRequest();
                WebUtil.addOriginalRequestUrl(exchanger,new URI(request.uri()));
                String newPath = request.uri().replaceAll(config.regexp, replacement);
                exchanger.context().put(WebUtil.GATEWAY_REQUEST_URL_ATTR, newPath);
            }catch (Exception e){
                WebUtil.setBadStatus(exchanger.getResponse(),e);
            }
            filterChain.filter(exchanger);
        };
    }

    public static class Config {

        private String regexp;

        private String replacement;

        public String getRegexp() {
            return regexp;
        }

        public Config setRegexp(String regexp) {
            Assert.hasText(regexp, "regexp must have a value");
            this.regexp = regexp;
            return this;
        }

        public String getReplacement() {
            return replacement;
        }

        public Config setReplacement(String replacement) {
            Assert.notNull(replacement, "replacement must not be null");
            this.replacement = replacement;
            return this;
        }

    }

    public static void main(String[] args) throws URISyntaxException {
        URI uri = new URI("http://localhost:8888/baidu");
        System.out.println(uri.getRawPath());
    }
}
