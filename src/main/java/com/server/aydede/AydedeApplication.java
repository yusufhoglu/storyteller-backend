package com.server.aydede;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AydedeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AydedeApplication.class, args);
	}

}
