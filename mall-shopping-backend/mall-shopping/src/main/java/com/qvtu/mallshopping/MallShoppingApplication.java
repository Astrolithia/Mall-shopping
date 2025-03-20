package com.qvtu.mallshopping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.qvtu.mallshopping.model")
@EnableJpaRepositories("com.qvtu.mallshopping.repository")
@ComponentScan(basePackages = "com.qvtu.mallshopping")
public class MallShoppingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallShoppingApplication.class, args);
    }

}
