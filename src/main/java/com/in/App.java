package com.in;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableAutoConfiguration
@ComponentScan
@EnableJpaRepositories
@EnableScheduling
public class App {
    public static void main(String[] args) {
        // Set default timezone to IST
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        
        // For production deployment
        System.setProperty("server.port", System.getenv().getOrDefault("PORT", "9090"));
        
        SpringApplication.run(App.class, args);
    }
}