package com.nyihtuun.bentosystem.planmanagementservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PlanManagementServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanManagementServiceApplication.class, args);
	}

}
