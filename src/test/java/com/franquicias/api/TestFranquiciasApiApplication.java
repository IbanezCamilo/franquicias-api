package com.franquicias.api;

import org.springframework.boot.SpringApplication;

public class TestFranquiciasApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(FranquiciasApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
