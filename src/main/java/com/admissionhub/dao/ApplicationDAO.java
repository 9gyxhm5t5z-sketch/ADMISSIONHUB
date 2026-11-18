package com.admissionhub.dao;

import com.admissionhub.model.AdmissionStatus;
import com.admissionhub.model.Application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    public int addApplication(Application a) throws SQLException {
        String sql = "INSERT INTO applications (student_id, program, status) " +
                "VALUES (?, ?, ?) RETURNING application_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getStudentId());
            ps.setString(2, a.getProgram());
            ps.setString(3, a.getStatus().name());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public List<Application> getAllApplications() throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT * FROM applications ORDER BY application_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Application> getApplicationsByStudent(int studentId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT * FROM applications WHERE student_id = ? ORDER BY application_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Application getApplicationById(int id) throws SQLException {
        String sql = "SELECT * FROM applications WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public boolean updateApplication(Application a) throws SQLException {
        String sql = "UPDATE applications SET program=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE application_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getProgram());
            ps.setString(2, a.getStatus().name());
            ps.setInt(3, a.getApplicationId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int applicationId, AdmissionStatus newStatus) throws SQLException {
        String sql = "UPDATE applications SET status=?, updated_at=CURRENT_TIMESTAMP WHERE application_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteApplication(int id) throws SQLException {
        String sql = "DELETE FROM applications WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Application mapRow(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setApplicationId(rs.getInt("application_id"));
        a.setStudentId(rs.getInt("student_id"));
        a.setProgram(rs.getString("program"));
        a.setStatus(AdmissionStatus.fromString(rs.getString("status")));
        Date applied = rs.getDate("applied_on");
        if (applied != null) a.setAppliedOn(applied.toLocalDate());
        return a;
    }
}