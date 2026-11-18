package com.admissionhub.model;

public enum AdmissionStatus {
    PENDING, UNDER_REVIEW, APPROVED, REJECTED;

    public static AdmissionStatus fromString(String value) {
        return AdmissionStatus.valueOf(value.toUpperCase());
    }
}