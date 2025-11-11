package com.example;

import com.example.client.RpcServiceRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import(RpcServiceRegistrar.class)
@Configuration
@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class ClientMain {
    public static void main(String[] args) {
//        System.out.printf("Hello and welcome!");
        SpringApplication.run(ClientMain.class, args);
    }
}