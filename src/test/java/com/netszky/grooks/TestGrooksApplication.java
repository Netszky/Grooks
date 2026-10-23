package com.netszky.grooks;

import org.springframework.boot.SpringApplication;

public class TestGrooksApplication {

	public static void main(String[] args) {
		SpringApplication.from(GrooksApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
