package com.admissionhub.model;

import java.time.LocalDate;

public class PassportDetails {
    private int passportId;
    private int studentId;
    private String nationality;
    private String passportNumber;
    private String countryOfIssue;
    private String placeOfIssue;
    private LocalDate dateOfIssue;
    private LocalDate expiryDate;
    private String photoFilePath;
    private boolean skipped;

    public PassportDetails() {}

    public int getPassportId() { return passportId; }
    public void setPassportId(int passportId) { this.passportId = passportId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getPassportNumber() { return passportNumber; }
    public void setPassportNumber(String passportNumber) { this.passportNumber = passportNumber; }

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