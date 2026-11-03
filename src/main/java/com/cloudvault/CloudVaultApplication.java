package com.cloudvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CloudVaultApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(CloudVaultApplication.class, args);
        System.out.println("CloudVault Application Started Successfully!");
        System.out.println("Visit: http://localhost:8080");
    }
}