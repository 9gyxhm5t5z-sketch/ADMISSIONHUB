package com.admissionhub.model;

import java.time.LocalDate;

public class Student {
    private int studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String whatsappContact;
    private String gender;
    private LocalDate dob;
    private String placeOfBirth;
    private String countryOfBirth;
    private String address;
    private String courseApplying;
    private boolean profileCompleted;

    public Student() {}

    public Student(String firstName, String lastName, String email,
                   String gender, String whatsappContact, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.gender = gender;
        this.whatsappContact = whatsappContact;
        this.phone = phone;
        this.profileCompleted = false;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWhatsappContact() { return whatsappContact; }
    public void setWhatsappContact(String whatsappContact) { this.whatsappContact = whatsappContact; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }

    public String getCountryOfBirth() { return countryOfBirth; }
    public void setCountryOfBirth(String countryOfBirth) { this.countryOfBirth = countryOfBirth; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCourseApplying() { return courseApplying; }
    public void setCourseApplying(String courseApplying) { this.courseApplying = courseApplying; }

    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }

    public String getFullName() { return firstName + " " + lastName; }

    @Override
    public String toString() {
        return studentId + " - " + getFullName() + " (" + email + ")";
    }
}