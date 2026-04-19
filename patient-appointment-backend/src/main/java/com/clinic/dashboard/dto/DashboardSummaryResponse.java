package com.clinic.dashboard.dto;

public class DashboardSummaryResponse {

    private long totalPatients;
    private long totalAppointments;
    private long todayAppointments;
    private long totalUsers;

    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(long totalPatients, long totalAppointments, long todayAppointments, long totalUsers) {
        this.totalPatients = totalPatients;
        this.totalAppointments = totalAppointments;
        this.todayAppointments = todayAppointments;
        this.totalUsers = totalUsers;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public long getTotalAppointments() {
        return totalAppointments;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public void setTotalAppointments(long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}