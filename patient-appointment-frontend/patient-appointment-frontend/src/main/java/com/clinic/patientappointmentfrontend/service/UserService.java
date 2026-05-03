package com.clinic.patientappointmentfrontend.service;

import com.clinic.patientappointmentfrontend.user.UserModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UserService {

    public List<UserModel> getAllUsers(String basicAuthToken) {
        try {
            URL url = new URL("http://localhost:8080/api/users");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);

            int responseCode = connection.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                return List.of();
            }

            InputStream inputStream = connection.getInputStream();
            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            StringBuilder response = new StringBuilder();

            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return parseUsers(response.toString());

        } catch (IOException e) {
            return List.of();
        }
    }

    public boolean createUser(String basicAuthToken, String fullName, String email, String password, String role) {
        try {
            URL url = new URL("http://localhost:8080/api/users");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = String.format("""
                    {
                      "fullName": "%s",
                      "email": "%s",
                      "password": "%s",
                      "role": "%s"
                    }
                    """, fullName, email, password, role);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 300;

        } catch (IOException e) {
            return false;
        }
    }

    private List<UserModel> parseUsers(String json) {
        List<UserModel> users = new ArrayList<>();

        if (json == null || json.isBlank() || json.equals("[]")) {
            return users;
        }

        String trimmed = json.substring(1, json.length() - 1);
        String[] objects = trimmed.split("\\},\\s*\\{");

        for (String object : objects) {
            String obj = object;
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            UserModel user = new UserModel();
            user.setId(extractLong(obj, "id"));
            user.setFullName(extractString(obj, "fullName"));
            user.setEmail(extractString(obj, "email"));
            user.setRole(extractString(obj, "role"));

            users.add(user);
        }

        return users;
    }

    public boolean deleteUser(String basicAuthToken, Long userId) {
        try {
            URL url = new URL("http://localhost:8080/api/users/" + userId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);

            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 300;

        } catch (IOException e) {
            return false;
        }
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return "";

        start += search.length();

        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        if (start < json.length() && json.charAt(start) == 'n') {
            return "";
        }

        if (start < json.length() && json.charAt(start) == '"') {
            start++;
            int end = json.indexOf("\"", start);
            if (end == -1) return "";
            return json.substring(start, end);
        }

        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        if (end == -1) return "";

        return json.substring(start, end).trim();
    }

    private Long extractLong(String json, String key) {
        String value = extractString(json, key);
        if (value == null || value.isBlank()) return null;
        return Long.parseLong(value);
    }
}