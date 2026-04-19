package com.clinic.patientappointmentfrontend.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class AuthService {

    public String login(String email, String password) {
        try {
            URL url = new URL("http://localhost:8080/api/auth/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = String.format("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """, email, password);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            Scanner scanner;
            if (responseCode >= 200 && responseCode < 300) {
                scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8);
            } else {
                scanner = new Scanner(connection.getErrorStream(), StandardCharsets.UTF_8);
            }

            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            if (responseCode >= 200 && responseCode < 300) {
                return "SUCCESS:" + response;
            } else {
                return "ERROR:" + response;
            }

        } catch (IOException e) {
            return "ERROR:Cannot connect to backend";
        }
    }

    public String encodeBasicAuth(String email, String password) {
        String value = email + ":" + password;
        return java.util.Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}