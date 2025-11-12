package com.innowise.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InternshipAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(InternshipAuthServiceApplication.class, args);
    }
}
