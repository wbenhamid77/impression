package com.example.Impression;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImpressionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImpressionApplication.class, args);
	}

}
