package com.in;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
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