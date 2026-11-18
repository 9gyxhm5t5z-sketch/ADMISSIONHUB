package com.admissionhub.dao;

import com.admissionhub.model.EditRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EditRequestDAO {

    public int createRequest(int studentId, String section) throws SQLException {
        String sql = "INSERT INTO edit_requests (student_id, section, status) " +
                "VALUES (?, ?, 'PENDING') RETURNING request_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, section);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public boolean hasPendingRequest(int studentId, String section) throws SQLException {
        String sql = "SELECT 1 FROM edit_requests WHERE student_id=? AND section=? AND status='PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, section);
            return ps.executeQuery().next();
        }
    }

    public EditRequest getApprovedUnusedRequest(int studentId, String section) throws SQLException {
        String sql = "SELECT * FROM edit_requests WHERE student_id=? AND section=? AND status='APPROVED' " +
                "ORDER BY decided_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, section);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void markUsed(int requestId) throws SQLException {
        String sql = "UPDATE edit_requests SET status='USED' WHERE request_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    public void approve(int requestId) throws SQLException {
        String sql = "UPDATE edit_requests SET status='APPROVED', decided_at=CURRENT_TIMESTAMP WHERE request_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    public void reject(int requestId) throws SQLException {
        String sql = "UPDATE edit_requests SET status='REJECTED', decided_at=CURRENT_TIMESTAMP WHERE request_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    public List<EditRequest> getAllPending() throws SQLException {
        List<EditRequest> list = new ArrayList<>();
        String sql = "SELECT er.*, s.first_name, s.last_name FROM edit_requests er " +
                "JOIN students s ON er.student_id = s.student_id " +
                "WHERE er.status = 'PENDING' ORDER BY er.requested_at";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                EditRequest r = mapRow(rs);
                r.setStudentName(rs.getString("first_name") + " " + rs.getString("last_name"));
                list.add(r);
            }
        }
        return list;
    }

    public List<EditRequest> getUndismissedDecisions(int studentId) throws SQLException {
        List<EditRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM edit_requests WHERE student_id=? AND status IN ('APPROVED','REJECTED') " +
                "AND notified = FALSE ORDER BY decided_at";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public void markNotified(int requestId) throws SQLException {
        String sql = "UPDATE edit_requests SET notified = TRUE WHERE request_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        }
    }

    private EditRequest mapRow(ResultSet rs) throws SQLException {
        EditRequest r = new EditRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setSection(rs.getString("section"));
        r.setStatus(rs.getString("status"));
        Timestamp reqAt = rs.getTimestamp("requested_at");
        if (reqAt != null) r.setRequestedAt(reqAt.toLocalDateTime());
        Timestamp decAt = rs.getTimestamp("decided_at");
        if (decAt != null) r.setDecidedAt(decAt.toLocalDateTime());
        return r;
    }
}