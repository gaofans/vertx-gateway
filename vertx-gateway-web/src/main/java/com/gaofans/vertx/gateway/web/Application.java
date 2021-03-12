package com.gaofans.vertx.gateway.web;

import com.gaofans.vertx.gateway.filter.GlobalFilter;
import com.gaofans.vertx.gateway.web.filter.HttpRoutingFilter;
import com.gaofans.vertx.gateway.web.handler.HttpHandlerAdapter;
import com.gaofans.vertx.gateway.web.handler.HttpFilteringHandler;
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
        filters.add(new HttpRoutingFilter(vertx));
        HttpFilteringHandler handler = new HttpFilteringHandler(filters);
        HttpHandlerAdapter handlerAdapter = new HttpHandlerAdapter(handler);

        vertx.createHttpServer().requestHandler(handlerAdapter).listen(8888);
    }
}
