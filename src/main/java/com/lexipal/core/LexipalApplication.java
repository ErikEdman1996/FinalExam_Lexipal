package com.lexipal.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class LexipalApplication {

	public static void main(String[] args) {
		SpringApplication.run(LexipalApplication.class, args);
	}

}
