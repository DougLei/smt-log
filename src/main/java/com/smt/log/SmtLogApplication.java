package com.smt.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

import com.douglei.orm.spring.boot.starter.TransactionComponentScan;

/**
 * 
 * @author DougLei
 */
@EnableEurekaClient
@SpringBootApplication
@ComponentScan("com.smt")
@TransactionComponentScan(packages = "com.smt")
public class SmtLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmtLogApplication.class, args);
	}
}
