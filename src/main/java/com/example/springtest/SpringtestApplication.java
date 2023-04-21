package com.example.springtest;

import java.util.List;
import java.util.Queue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import com.example.springtest.other.Breached;
import com.example.springtest.other.LeakedData;
import com.example.springtest.other.WebScrapingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
@RestController
public class SpringtestApplication {
  private final WebScrapingService webScrapingService= new WebScrapingService();;
  public static void main(String[] args) {
    SpringApplication.run(SpringtestApplication.class, args);
  }

  @GetMapping("/")
	public String Welcome(@RequestParam(value = "name", defaultValue = "User") String name) {
    Breached user = new Breached();
    user.setName(name);
    return "Welcome, " + name;
	}

  @GetMapping("/find-leaked-data")
    public Queue<LeakedData> findLeakedData(@RequestParam String seedUrl, @RequestParam int maxNodes) {
        return webScrapingService.findLeakedData(seedUrl, maxNodes);
    }

}