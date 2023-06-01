package com.example.springtest.model;

import jakarta.persistence.*;


@Entity
@Table(name = "websites")
public class Website {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;

    public Website() {
    }

    public Website(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
}
