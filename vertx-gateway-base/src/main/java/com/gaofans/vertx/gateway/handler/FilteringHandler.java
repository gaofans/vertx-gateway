package com.gaofans.vertx.gateway.handler;

/**
 * 对请求开始进行处理的接口
 * @author GaoFans
 * @param <T> 请求体
 * @param <R> 响应体
 */
public interface FilteringHandler<T, R> {

    /**
     * 处理请求与响应
     * @param exchanger 请求响应交换器
     */
    void handle(Exchanger<T,R> exchanger);

}
