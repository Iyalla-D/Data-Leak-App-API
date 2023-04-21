package com.example.springtest.other;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;


@RestController
public class HaveIBeenPwnedController {
    private static final String API_Key = "";
    private static final String API_URL = "https://api.pwnedpasswords.com/range/";
    private static final Gson gson = new Gson();


    public static List<Breached> getDataBreaches(String email) throws Exception {
        String url = API_URL + "/breachedaccount/" + email;
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("hibp-api-key", API_Key);
        int statusCode = con.getResponseCode();
        if (statusCode == 200) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            Type listType = new TypeToken<List<Breached>>(){}.getType();
            List<Breached> dataBreaches = gson.fromJson(in, listType);
            in.close();
            return dataBreaches;
        } else if (statusCode == 404) {
            return Collections.emptyList();
        } else {
            throw new RuntimeException("API request failed with status code " + statusCode);
        }

    }

    public static boolean isEmailPwned(String email) {
        try {
            List<Breached> dataBreaches = getDataBreaches(email);
            return !dataBreaches.isEmpty();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int getCountOfPasswordPwned(String password) {
    	try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashString = bytesToHex(hash);
            String hashPrefix = hashString.substring(0, 5).toUpperCase();
            String url = API_URL + hashPrefix;
            HttpGet request = new HttpGet(url);
            request.addHeader("hibp-api-key", API_Key);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(request);            
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            String[] lines = responseBody.split("\\r?\\n");
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts[0].equals(hashString.substring(5).toUpperCase())) {
                    return Integer.parseInt(parts[1]);
                }
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static boolean checkPwnedPass(String password) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] hash = sha1.digest(password.getBytes(StandardCharsets.UTF_8));
            String hashString = bytesToHex(hash);
            String hashPrefix = hashString.substring(0, 5).toUpperCase();
            String url = API_URL + hashPrefix;
            HttpGet request = new HttpGet(url);
            request.addHeader("hibp-api-key", API_Key);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);
            return responseBody.contains(hashString.substring(5).toUpperCase());
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }


    @PostMapping("/ispasswordpwned")
    public static boolean isPasswordPwned(@RequestBody Map<String, String> sentRequest) {
        String password = PasswordEncryptor.decryptObj(sentRequest);
        return checkPwnedPass(password);
    }

    public static boolean isPasswordPwnedSecondary(String password) {
        return checkPwnedPass(password);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
