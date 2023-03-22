package com.example.springtest.other;

import java.util.List;

import lombok.Data;

@Data
public class Breached {
    private String name;
    private String title;
    private String domain;
    private String breachDate;
    private String description;
    private List<String> dataClasses;
    private String pwnCount;
    private String logoPath;
}
