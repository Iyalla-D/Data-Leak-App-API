package com.example.springtest.other;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class BreachDirectory {

    public boolean sendRequest(String email,String pass) {
        try {
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://breachdirectory.p.rapidapi.com/?func=auto&term=" + encodedEmail))
                .header("X-RapidAPI-Key", "e32811fe08mshe9400947fdec850p175356jsn251759f13d71")
                .header("X-RapidAPI-Host", "breachdirectory.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return checkPassword(response.body(), pass);
            //System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkPassword(String response, String givenPassword) {
        Gson gson = new Gson();
        JsonObject responseObject = gson.fromJson(response, JsonObject.class);
        JsonArray results = responseObject.getAsJsonArray("result");

        for (JsonElement resultElement : results) {
            JsonObject result = resultElement.getAsJsonObject();
            boolean emailOnly = result.get("email_only").getAsBoolean();
            
            if (!emailOnly) {
                String line = result.get("line").getAsString();
                String[] splitLine = line.split(":");
                if (splitLine.length > 1) {
                    String password = splitLine[1];
                    if (password.equals(givenPassword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
