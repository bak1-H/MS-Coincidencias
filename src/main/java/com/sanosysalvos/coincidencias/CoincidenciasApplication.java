package com.sanosysalvos.coincidencias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.sanosysalvos.coincidencias", "com.controller"})
public class CoincidenciasApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoincidenciasApplication.class, args);
	}

}
