package com.gaofans.vertx.gateway.handler;

public interface FilteringHandler<T, R> {

    void handle(Exchanger<T,R> exchanger);

}
