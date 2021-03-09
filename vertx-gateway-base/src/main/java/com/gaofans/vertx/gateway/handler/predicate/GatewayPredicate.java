package com.gaofans.vertx.gateway.handler.predicate;

import com.gaofans.vertx.gateway.handler.Exchanger;
import org.springframework.util.Assert;

import java.util.function.Predicate;

/**
 * 网关转发条件判断接口
 * @author GaoFans
 * @since 2021/2/27
 */
public interface GatewayPredicate<T,R> extends Predicate<Exchanger<T,R>> {

    /**
     * 与
     * @param other 其他条件
     * @return 组合结果
     */
    @Override
    default Predicate<Exchanger<T, R>> and(Predicate<? super Exchanger<T, R>> other) {
        return new AndGatewayPredicate<>(this, wrapIfNeeded(other));
    }

    /**
     * 否
     * @return 当前取反
     */
    @Override
    default Predicate<Exchanger<T, R>> negate() {
        return new NegateGatewayPredicate<>(this);
    }

    /**
     * 或
     * @param other 其他条件
     * @return 组合结果
     */
    @Override
    default Predicate<Exchanger<T, R>> or(Predicate<? super Exchanger<T, R>> other) {
        return new OrGatewayPredicate<>(this,wrapIfNeeded(other));
    }

    /**
     * 将java Predicate 转为网关Predicate
     * @param other 需要转换的条件
     * @param <T> request
     * @param <R> response
     * @return 网关Predicate
     */
    @SuppressWarnings("unchecked")
    static <T,R> GatewayPredicate<T,R> wrapIfNeeded(Predicate<? super Exchanger<T,R>> other) {
        GatewayPredicate<T,R> right;

        if (other instanceof GatewayPredicate) {
            right = (GatewayPredicate) other;
        }else {
            right = new GatewayPredicateWrapper<>(other);
        }
        return right;
    }

    class GatewayPredicateWrapper<T,R> implements GatewayPredicate<T,R> {

        private final Predicate<? super Exchanger<T,R>> delegate;

        public GatewayPredicateWrapper(Predicate<? super Exchanger<T,R>> delegate) {
            Assert.notNull(delegate, "delegate GatewayPredicate must not be null");
            this.delegate = delegate;
        }

        @Override
        public boolean test(Exchanger<T,R> exchange) {
            return this.delegate.test(exchange);
        }

        @Override
        public String toString() {
            return this.delegate.getClass().getSimpleName();
        }

    }

    class NegateGatewayPredicate<T,R> implements GatewayPredicate<T,R> {

        private final GatewayPredicate<T,R> predicate;

        public NegateGatewayPredicate(GatewayPredicate<T,R> predicate) {
            Assert.notNull(predicate, "predicate GatewayPredicate must not be null");
            this.predicate = predicate;
        }

        @Override
        public boolean test(Exchanger<T,R> t) {
            return !this.predicate.test(t);
        }

        @Override
        public String toString() {
            return String.format("!%s", this.predicate);
        }

    }

    class AndGatewayPredicate<T,R> implements GatewayPredicate<T,R> {

        private final GatewayPredicate<T,R> left;

        private final GatewayPredicate<T,R> right;

        public AndGatewayPredicate(GatewayPredicate<T,R> left, GatewayPredicate<T,R> right) {
            Assert.notNull(left, "Left GatewayPredicate must not be null");
            Assert.notNull(right, "Right GatewayPredicate must not be null");
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean test(Exchanger<T,R> t) {
            return (this.left.test(t) && this.right.test(t));
        }

        @Override
        public String toString() {
            return String.format("(%s && %s)", this.left, this.right);
        }

    }

    class OrGatewayPredicate<T,R> implements GatewayPredicate<T,R> {

        private final GatewayPredicate<T,R> left;

        private final GatewayPredicate<T,R> right;

        public OrGatewayPredicate(GatewayPredicate<T,R> left, GatewayPredicate<T,R> right) {
            Assert.notNull(left, "Left GatewayPredicate must not be null");
            Assert.notNull(right, "Right GatewayPredicate must not be null");
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean test(Exchanger<T,R> t) {
            return (this.left.test(t) || this.right.test(t));
        }

        @Override
        public String toString() {
            return String.format("(%s || %s)", this.left, this.right);
        }

    }
}
