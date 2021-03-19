package com.gaofans.vertx.gateway.handler;

import com.gaofans.vertx.gateway.route.Route;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

/**
 * 代理请求与响应之间的交换器
 * @author GaoFans
 * @param <T> 需要代理的请求
 * @param <R> 代理返回的响应
 */
public interface Exchanger<T,R> {

    /**
     * 获取请求
     * @return request
     */
    T getRequest();

    /**
     * 获取响应
     * @return response
     */
    R getResponse();

    /**
     * 是否已经路由
     * @return bool
     */
    boolean isRouted();

    /**
     * 设置是否路由
     * @param routed 是否
     */
    void setRouted(boolean routed);

    /**
     * 获取当前路由
     * @return route
     */
    Route<T,R> getRoute();

    /**
     * 获取上下文
     * @return context
     */
    Map<String, Object> context();

    /**
     * 获取上下文中的属性
     * @param name 属性名称
     * @param <M> 属性值
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    default <M> M context(String name) {
        return (M) Optional.ofNullable(context().get(name)).orElse(null);
    }

    /**
     * 获取上下文中的属性
     * @param name 属性名称
     * @param defaultValue 默认值
     * @param <M> 属性值
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    default <M> M context(String name,M defaultValue) {
        return Optional.ofNullable(context().get(name)).map(o -> (M)o).orElse(defaultValue);
    }

    /**
     * 获取上下文中必须的属性
     * @param name 属性名称
     * @param <M> 属性值
     * @return 属性值
     */
    default <M> M requiredContext(String name) {
        M value = context(name);
        Assert.notNull(value, () -> "Required attribute '" + name + "' is missing");
        return value;
    }
}
