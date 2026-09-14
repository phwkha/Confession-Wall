package com.example.confessionwall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConfessionWallApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfessionWallApplication.class, args);
    }
}
