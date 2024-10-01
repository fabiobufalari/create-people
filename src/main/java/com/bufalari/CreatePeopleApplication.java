package com.bufalari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = "com.bufalari") // Adicione esta linha
public class CreatePeopleApplication {
	public static void main(String[] args) {
		SpringApplication.run(CreatePeopleApplication.class, args);
	}
}