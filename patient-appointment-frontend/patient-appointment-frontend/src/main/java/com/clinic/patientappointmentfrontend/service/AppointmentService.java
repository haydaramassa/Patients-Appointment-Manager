package com.clinic.patientappointmentfrontend.service;

import com.clinic.patientappointmentfrontend.appointment.AppointmentModel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AppointmentService {

    private String lastErrorMessage = "";

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

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

    public boolean createAppointment(String basicAuthToken,
                                     Long patientId,
                                     String appointmentDateTime,
                                     String status,
                                     String doctorName,
                                     String notes) {
        lastErrorMessage = "";

        try {
            URL url = new URL("http://localhost:8080/api/appointments");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = buildAppointmentJson(patientId, appointmentDateTime, status, doctorName, notes);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                return true;
            }

            lastErrorMessage = getReadableError(responseBody);
            return false;

        } catch (IOException e) {
            lastErrorMessage = "Cannot connect to backend";
            return false;
        }
    }

    public boolean updateAppointment(String basicAuthToken,
                                     Long appointmentId,
                                     Long patientId,
                                     String appointmentDateTime,
                                     String status,
                                     String doctorName,
                                     String notes) {
        lastErrorMessage = "";

        try {
            URL url = new URL("http://localhost:8080/api/appointments/" + appointmentId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = buildAppointmentJson(patientId, appointmentDateTime, status, doctorName, notes);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                return true;
            }

            lastErrorMessage = getReadableError(responseBody);
            return false;

        } catch (IOException e) {
            lastErrorMessage = "Cannot connect to backend";
            return false;
        }
    }

    public boolean deleteAppointment(String basicAuthToken, Long appointmentId) {
        lastErrorMessage = "";

        try {
            URL url = new URL("http://localhost:8080/api/appointments/" + appointmentId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);

            int responseCode = connection.getResponseCode();
            String responseBody = readResponseBody(connection, responseCode);

            if (responseCode >= 200 && responseCode < 300) {
                return true;
            }

            lastErrorMessage = getReadableError(responseBody);
            return false;

        } catch (IOException e) {
            lastErrorMessage = "Cannot connect to backend";
            return false;
        }
    }

    private String readResponseBody(HttpURLConnection connection, int responseCode) {
        try {
            InputStream stream;

            if (responseCode >= 200 && responseCode < 300) {
                stream = connection.getInputStream();
            } else {
                stream = connection.getErrorStream();
            }

            if (stream == null) {
                return "";
            }

            Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8);
            StringBuilder response = new StringBuilder();

            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }

            scanner.close();
            return response.toString();

        } catch (IOException e) {
            return "";
        }
    }

    private String getReadableError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Operation failed. Please try again.";
        }

        if (responseBody.contains("An appointment already exists at this date and time")) {
            return "This time slot is already booked. Please choose another time.";
        }

        if (responseBody.contains("Patient not found")) {
            return "Selected patient was not found.";
        }

        if (responseBody.contains("Appointment not found")) {
            return "Appointment was not found.";
        }

        return "Operation failed. Please check the entered information.";
    }

    private String buildAppointmentJson(Long patientId,
                                        String appointmentDateTime,
                                        String status,
                                        String doctorName,
                                        String notes) {
        return String.format("""
                {
                  "patientId": %d,
                  "appointmentDateTime": "%s",
                  "status": "%s",
                  "doctorName": "%s",
                  "notes": "%s"
                }
                """,
                patientId,
                escapeJson(appointmentDateTime),
                escapeJson(status),
                escapeJson(doctorName),
                escapeJson(notes)
        );
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

            if (!obj.startsWith("{")) {
                obj = "{" + obj;
            }

            if (!obj.endsWith("}")) {
                obj = obj + "}";
            }

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