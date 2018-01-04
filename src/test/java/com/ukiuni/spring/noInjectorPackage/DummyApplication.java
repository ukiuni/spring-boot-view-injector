package com.ukiuni.spring.noInjectorPackage;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@SpringBootApplication
@RestController
public class DummyApplication {
	public static void main(String[] args) {
		SpringApplication.run(DummyApplication.class, args);
	}

	@GetMapping("api")
	public List<Data> loadData() {
		return Arrays.asList(new Data(1, "test1"), new Data(2, "test2"));
	}

	@GetMapping("owned")
	public Data loadOwnedData(@RequestHeader("Authorization") String authHeader) {
		return new Data(0, authHeader);
	}

	@lombok.Data
	@AllArgsConstructor
	public class Data {
		private long id;
		private String arguments;
	}
}
