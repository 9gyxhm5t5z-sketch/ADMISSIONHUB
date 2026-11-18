package com.admissionhub.model;

import java.time.LocalDate;

public class Application {
    private int applicationId;
    private int studentId;
    private String program;
    private AdmissionStatus status;
    private LocalDate appliedOn;

    public Application() {}

    public Application(int studentId, String program, AdmissionStatus status) {
        this.studentId = studentId;
        this.program = program;
        this.status = status;
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }

    public AdmissionStatus getStatus() { return status; }
    public void setStatus(AdmissionStatus status) { this.status = status; }

    public LocalDate getAppliedOn() { return appliedOn; }
    public void setAppliedOn(LocalDate appliedOn) { this.appliedOn = appliedOn; }

    @Override
    public String toString() {
        return applicationId + " - Student#" + studentId + " - " + program + " [" + status + "]";
    }
}