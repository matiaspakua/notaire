package com.licensis.notaire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.licensis.notaire")
public class NotaireBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotaireBackendApplication.class, args);
    }
}
