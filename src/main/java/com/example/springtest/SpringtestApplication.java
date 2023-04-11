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
    @GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name,
                    @RequestParam(value = "password") String password) {
    Breached user = new Breached();
    user.setName(name);
    return name;
	}
}