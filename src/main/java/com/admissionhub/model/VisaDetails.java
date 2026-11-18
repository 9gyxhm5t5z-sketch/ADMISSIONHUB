package com.admissionhub.model;

import java.time.LocalDate;

public class VisaDetails {
    private int visaId;
    private int studentId;
    private String visaNumber;
    private String countryOfIssue;
    private String placeOfIssue;
    private LocalDate dateOfIssue;
    private LocalDate expiryDate;
    private String photoFilePath;
    private boolean skipped;

    public VisaDetails() {}

    public int getVisaId() { return visaId; }
    public void setVisaId(int visaId) { this.visaId = visaId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getVisaNumber() { return visaNumber; }
    public void setVisaNumber(String visaNumber) { this.visaNumber = visaNumber; }

    public String getCountryOfIssue() { return countryOfIssue; }
    public void setCountryOfIssue(String countryOfIssue) { this.countryOfIssue = countryOfIssue; }

    public String getPlaceOfIssue() { return placeOfIssue; }
    public void setPlaceOfIssue(String placeOfIssue) { this.placeOfIssue = placeOfIssue; }

    public LocalDate getDateOfIssue() { return dateOfIssue; }
    public void setDateOfIssue(LocalDate dateOfIssue) { this.dateOfIssue = dateOfIssue; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getPhotoFilePath() { return photoFilePath; }
    public void setPhotoFilePath(String photoFilePath) { this.photoFilePath = photoFilePath; }

    public boolean isSkipped() { return skipped; }
    public void setSkipped(boolean skipped) { this.skipped = skipped; }
}