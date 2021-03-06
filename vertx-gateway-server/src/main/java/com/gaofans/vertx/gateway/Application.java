package com.gaofans.vertx.gateway;

import io.vertx.core.Vertx;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author GaoFans
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }

    public void server(){
        Vertx vertx = Vertx.vertx();
        vertx.createHttpServer().requestHandler(req -> {

        });
    }
}
