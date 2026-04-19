package com.clinic.patientappointmentfrontend.service;

import com.clinic.patientappointmentfrontend.appointment.AppointmentModel;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppointmentService {

    public List<AppointmentModel> getAllAppointments(String basicAuthToken) {
        try {
            URL url = new URL("http://localhost:8080/api/appointments");
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

            return parseAppointments(response.toString());

        } catch (IOException e) {
            return List.of();
        }
    }

    private List<AppointmentModel> parseAppointments(String json) {
        List<AppointmentModel> appointments = new ArrayList<>();

        if (json == null || json.isBlank() || json.equals("[]")) {
            return appointments;
        }

        String trimmed = json.substring(1, json.length() - 1);
        String[] objects = trimmed.split("\\},\\s*\\{");

        for (String object : objects) {
            String obj = object;
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            AppointmentModel appointment = new AppointmentModel();
            appointment.setId(extractLong(obj, "id"));
            appointment.setPatientId(extractLong(obj, "patientId"));
            appointment.setPatientName(extractString(obj, "patientName"));
            appointment.setAppointmentDateTime(extractString(obj, "appointmentDateTime"));
            appointment.setStatus(extractString(obj, "status"));
            appointment.setDoctorName(extractString(obj, "doctorName"));
            appointment.setNotes(extractString(obj, "notes"));

            appointments.add(appointment);
        }

        return appointments;
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