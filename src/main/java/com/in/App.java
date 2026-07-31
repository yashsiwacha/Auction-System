package com.in;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication(scanBasePackages = {"com.in", "com.auction"})
@EntityScan(basePackages = {"com.in.entity", "com.auction.infrastructure.adapter.out.persistence.entity"})
@EnableJpaRepositories(basePackages = {"com.in.repository", "com.auction.infrastructure.adapter.out.persistence.repository"})
@EnableScheduling
public class App {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        SpringApplication.run(App.class, args);
    }
}