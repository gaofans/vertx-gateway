package com.gaofans.vertx.gateway.web;

import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.web.filter.VertxRoutingFilter;
import com.gaofans.vertx.gateway.web.handler.VertxHandlerAdapter;
import com.gaofans.vertx.gateway.web.handler.WebFilteringHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @PostConstruct
    public void start(){
        Vertx vertx = Vertx.vertx();
        List<GlobalFilter<HttpServerRequest, HttpServerResponse>> filters = new ArrayList<>();
        filters.add(new VertxRoutingFilter(vertx));
        WebFilteringHandler handler = new WebFilteringHandler(filters);
        VertxHandlerAdapter handlerAdapter = new VertxHandlerAdapter(handler);

        vertx.createHttpServer().requestHandler(handlerAdapter).listen(8888);
    }
}
