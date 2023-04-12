package com.example.springtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import com.example.springtest.other.Breached;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@RestController
public class SpringtestApplication {
  public static void main(String[] args) {
    SpringApplication.run(SpringtestApplication.class, args);
  }

  @GetMapping("/")
	public String Welcome(@RequestParam(value = "name", defaultValue = "User") String name) {
    Breached user = new Breached();
    user.setName(name);
    return "Welcome, " + name;
	}
}