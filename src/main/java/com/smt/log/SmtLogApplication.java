package com.smt.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 
 * @author DougLei
 */
@EnableEurekaClient
@SpringBootApplication
@ComponentScan("com.smt")
public class SmtLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmtLogApplication.class, args);
	}
}
