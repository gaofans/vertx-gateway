package com.gaofans.vertx.gateway.filter.factory;

import com.gaofans.vertx.gateway.support.AbstractConfigurable;

public abstract class AbstractGatewayFilterFactory<C,T,R> extends AbstractConfigurable<C> implements GatewayFilterFactory<C,T,R> {

    @SuppressWarnings("unchecked")
    public AbstractGatewayFilterFactory() {
        super((Class<C>) Object.class);
    }

    protected AbstractGatewayFilterFactory(Class<C> configClass) {
        super(configClass);
    }

    public static class NameConfig {

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
