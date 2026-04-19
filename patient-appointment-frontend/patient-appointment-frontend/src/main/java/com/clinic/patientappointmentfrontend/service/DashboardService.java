package com.clinic.patientappointmentfrontend.service;

import com.clinic.patientappointmentfrontend.dto.DashboardSummaryResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DashboardService {

    public DashboardSummaryResponse getSummary(String basicAuthToken) {
        try {
            URL url = new URL("http://localhost:8080/api/dashboard/summary");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Basic " + basicAuthToken);

            int responseCode = connection.getResponseCode();

            InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                return null;
            }

            Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
            StringBuilder response = new StringBuilder();

            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();

            return parseSummary(response.toString());

        } catch (IOException e) {
            return null;
        }
    }

    private DashboardSummaryResponse parseSummary(String json) {
        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalPatients(extractLong(json, "totalPatients"));
        response.setTotalAppointments(extractLong(json, "totalAppointments"));
        response.setTodayAppointments(extractLong(json, "todayAppointments"));
        response.setTotalUsers(extractLong(json, "totalUsers"));
        return response;
    }

    private long extractLong(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) {
            return 0;
        }

        start += search.length();
        int end = start;

        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }

        String value = json.substring(start, end).trim();
        if (value.isEmpty()) {
            return 0;
        }

        return Long.parseLong(value);
    }
}