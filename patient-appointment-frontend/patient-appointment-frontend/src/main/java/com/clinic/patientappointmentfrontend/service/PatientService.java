package com.clinic.patientappointmentfrontend.service;

import com.clinic.patientappointmentfrontend.patient.PatientModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PatientService {

    public List<PatientModel> getAllPatients(String basicAuthToken) {
        try {
            URL url = new URL("http://localhost:8080/api/patients");
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

            return parsePatients(response.toString());

        } catch (IOException e) {
            return List.of();
        }
    }

    public boolean createPatient(String basicAuthToken,
                                 String firstName,
                                 String lastName,
                                 String birthDate,
                                 String gender,
                                 String phone,
                                 String email,
                                 String address,
                                 String notes) {
        try {
            URL url = new URL("http://localhost:8080/api/patients");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = buildPatientJson(firstName, lastName, birthDate, gender, phone, email, address, notes);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            System.out.println("Create patient response code: " + responseCode);

            return responseCode >= 200 && responseCode < 300;

        } catch (IOException e) {
            System.out.println("Create patient error: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePatient(String basicAuthToken,
                                 Long patientId,
                                 String firstName,
                                 String lastName,
                                 String birthDate,
                                 String gender,
                                 String phone,
                                 String email,
                                 String address,
                                 String notes) {
        try {
            URL url = new URL("http://localhost:8080/api/patients/" + patientId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = buildPatientJson(firstName, lastName, birthDate, gender, phone, email, address, notes);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            System.out.println("Update patient response code: " + responseCode);

            return responseCode >= 200 && responseCode < 300;

        } catch (IOException e) {
            System.out.println("Update patient error: " + e.getMessage());
            return false;
        }
    }

    public boolean deletePatient(String basicAuthToken, Long patientId) {
        try {
            URL url = new URL("http://localhost:8080/api/patients/" + patientId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);

            int responseCode = connection.getResponseCode();

            System.out.println("Delete patient response code: " + responseCode);

            return responseCode >= 200 && responseCode < 300;

        } catch (IOException e) {
            System.out.println("Delete patient error: " + e.getMessage());
            return false;
        }
    }

    private String buildPatientJson(String firstName,
                                    String lastName,
                                    String birthDate,
                                    String gender,
                                    String phone,
                                    String email,
                                    String address,
                                    String notes) {
        return String.format("""
                {
                  "firstName": "%s",
                  "lastName": "%s",
                  "birthDate": "%s",
                  "gender": "%s",
                  "phone": "%s",
                  "email": "%s",
                  "address": "%s",
                  "notes": "%s"
                }
                """,
                escapeJson(firstName),
                escapeJson(lastName),
                escapeJson(birthDate),
                escapeJson(gender),
                escapeJson(phone),
                escapeJson(email),
                escapeJson(address),
                escapeJson(notes)
        );
    }

    private List<PatientModel> parsePatients(String json) {
        List<PatientModel> patients = new ArrayList<>();

        if (json == null || json.isBlank() || json.equals("[]")) {
            return patients;
        }

        String trimmed = json.substring(1, json.length() - 1);
        String[] objects = trimmed.split("\\},\\s*\\{");

        for (String object : objects) {
            String obj = object;

            if (!obj.startsWith("{")) {
                obj = "{" + obj;
            }

            if (!obj.endsWith("}")) {
                obj = obj + "}";
            }

            PatientModel patient = new PatientModel();
            patient.setId(extractLong(obj, "id"));
            patient.setFirstName(extractString(obj, "firstName"));
            patient.setLastName(extractString(obj, "lastName"));
            patient.setBirthDate(extractString(obj, "birthDate"));
            patient.setGender(extractString(obj, "gender"));
            patient.setPhone(extractString(obj, "phone"));
            patient.setEmail(extractString(obj, "email"));
            patient.setAddress(extractString(obj, "address"));
            patient.setNotes(extractString(obj, "notes"));

            patients.add(patient);
        }

        return patients;
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);

        if (start == -1) {
            return "";
        }

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

            if (end == -1) {
                return "";
            }

            return json.substring(start, end);
        }

        int end = json.indexOf(",", start);

        if (end == -1) {
            end = json.indexOf("}", start);
        }

        if (end == -1) {
            return "";
        }

        return json.substring(start, end).trim();
    }

    private Long extractLong(String json, String key) {
        String value = extractString(json, key);

        if (value == null || value.isBlank()) {
            return null;
        }

        return Long.parseLong(value);
    }
}