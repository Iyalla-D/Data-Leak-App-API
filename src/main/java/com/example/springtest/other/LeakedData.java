package com.example.springtest.other;

import lombok.Data;

@Data
public class LeakedData {
    private String url;
    private String email;
    private String password;

    public LeakedData(String url, String email, String password) {
        this.url = url;
        this.email = email;
        this.password = password;
    }
}
