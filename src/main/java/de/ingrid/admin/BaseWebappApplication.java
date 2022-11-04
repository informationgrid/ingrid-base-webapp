package de.ingrid.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(scanBasePackages = "de.ingrid")
public class BaseWebappApplication {

	/*@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(BaseWebappApplication.class);
	}*/

    public static void main(String[] args) {
        SpringApplication.run(BaseWebappApplication.class, args);
    }

}

