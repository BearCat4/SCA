package com.example.sca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ScaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScaApplication.class, args);
    }
}
