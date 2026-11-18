package com.admissionhub.dao;

import com.admissionhub.model.Student;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public int addStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (first_name, last_name, email, gender, whatsapp_contact, phone, profile_completed) " +
                "VALUES (?, ?, ?, ?, ?, ?, FALSE) RETURNING student_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getGender());
            ps.setString(5, s.getWhatsappContact());
            ps.setString(6, s.getPhone());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public boolean completeProfile(int studentId, LocalDate dob, String placeOfBirth,
                                   String countryOfBirth, String address, String courseApplying) throws SQLException {
        String sql = "UPDATE students SET dob=?, place_of_birth=?, country_of_birth=?, address=?, " +
                "course_applying=?, profile_completed=TRUE WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, dob != null ? Date.valueOf(dob) : null);
            ps.setString(2, placeOfBirth);
            ps.setString(3, countryOfBirth);
            ps.setString(4, address);
            ps.setString(5, courseApplying);
            ps.setInt(6, studentId);
            boolean ok = ps.executeUpdate() > 0;
            syncApplication(conn, studentId, courseApplying);
            return ok;
        }
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Student getStudentById(int id) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public boolean updateStudent(Student s) throws SQLException {
        String sql = "UPDATE students SET first_name=?, last_name=?, email=?, gender=?, " +
                "whatsapp_contact=?, phone=?, dob=?, place_of_birth=?, country_of_birth=?, address=?, course_applying=? " +
                "WHERE student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFirstName());
            ps.setString(2, s.getLastName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getGender());
            ps.setString(5, s.getWhatsappContact());
            ps.setString(6, s.getPhone());
            ps.setDate(7, s.getDob() != null ? Date.valueOf(s.getDob()) : null);
            ps.setString(8, s.getPlaceOfBirth());
            ps.setString(9, s.getCountryOfBirth());
            ps.setString(10, s.getAddress());
            ps.setString(11, s.getCourseApplying());
            ps.setInt(12, s.getStudentId());
            boolean ok = ps.executeUpdate() > 0;
            syncApplication(conn, s.getStudentId(), s.getCourseApplying());
            return ok;
        }
    }

    public boolean deleteStudent(int id) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void syncApplication(Connection conn, int studentId, String course) throws SQLException {
        if (course == null || course.trim().isEmpty()) return;
        String upd = "UPDATE applications SET program=?, updated_at=CURRENT_TIMESTAMP WHERE application_id = " +
                "(SELECT application_id FROM applications WHERE student_id=? ORDER BY application_id DESC LIMIT 1)";
        try (PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setString(1, course.trim());
            ps.setInt(2, studentId);
            if (ps.executeUpdate() > 0) return;
        }
        String ins = "INSERT INTO applications (student_id, program, status) VALUES (?, ?, 'PENDING')";
        try (PreparedStatement ps = conn.prepareStatement(ins)) {
            ps.setInt(1, studentId);
            ps.setString(2, course.trim());
            ps.executeUpdate();
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setGender(rs.getString("gender"));
        s.setWhatsappContact(rs.getString("whatsapp_contact"));
        s.setPhone(rs.getString("phone"));
        Date dob = rs.getDate("dob");
        if (dob != null) s.setDob(dob.toLocalDate());
        s.setPlaceOfBirth(rs.getString("place_of_birth"));
        s.setCountryOfBirth(rs.getString("country_of_birth"));
        s.setAddress(rs.getString("address"));
        s.setCourseApplying(rs.getString("course_applying"));
        s.setProfileCompleted(rs.getBoolean("profile_completed"));
        return s;
    }
}