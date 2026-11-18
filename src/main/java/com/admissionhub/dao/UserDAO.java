package com.admissionhub.dao;

import com.admissionhub.model.User;
import com.admissionhub.model.UserRole;

import java.sql.*;

public class UserDAO {

    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                u.setPassword(rs.getString("password"));
                u.setRole(UserRole.valueOf(rs.getString("role")));
                int studentId = rs.getInt("student_id");
                u.setStudentId(rs.wasNull() ? null : studentId);
                return u;
            }
        }
        return null;
    }

    public int createStudentUser(String username, String password, int studentId) throws SQLException {
        String sql = "INSERT INTO users (username, password, role, student_id) " +
                "VALUES (?, ?, 'STUDENT', ?) RETURNING user_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setInt(3, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            return ps.executeQuery().next();
        }
    }
}