package com.admissionhub.model;

import java.time.LocalDateTime;

public class Document {
    private int documentId;
    private Integer studentId;
    private Integer applicationId;
    private String docType;
    private String filePath;
    private LocalDateTime uploadedOn;
    private boolean verified;

    public Document() {}

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }

    public Integer getApplicationId() { return applicationId; }
    public void setApplicationId(Integer applicationId) { this.applicationId = applicationId; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getUploadedOn() { return uploadedOn; }
    public void setUploadedOn(LocalDateTime uploadedOn) { this.uploadedOn = uploadedOn; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    @Override
    public String toString() {
        return documentId + " - " + docType + (verified ? " (verified)" : " (unverified)");
    }
}