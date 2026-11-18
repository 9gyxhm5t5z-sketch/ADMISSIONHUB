package com.admissionhub.dao;

import java.sql.*;

public class DashboardDAO {

    public int getTotalStudents() throws SQLException {
        return runCount("SELECT COUNT(*) FROM students");
    }

    public int getTotalApplications() throws SQLException {
        return runCount("SELECT COUNT(*) FROM applications");
    }

    public int getApplicationCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM applications WHERE status = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public int getTotalDocuments() throws SQLException {
        return runCount("SELECT COUNT(*) FROM documents");
    }

    public int getUnverifiedDocuments() throws SQLException {
        return runCount("SELECT COUNT(*) FROM documents WHERE verified = FALSE");
    }

    private int runCount(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}