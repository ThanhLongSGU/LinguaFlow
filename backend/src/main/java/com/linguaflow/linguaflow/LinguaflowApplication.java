package com.linguaflow.linguaflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LinguaflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinguaflowApplication.class, args);
	}

}
