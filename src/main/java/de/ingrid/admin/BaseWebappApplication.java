package de.ingrid.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "de.ingrid")
public class BaseWebappApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(BaseWebappApplication.class, args);
    }

}

