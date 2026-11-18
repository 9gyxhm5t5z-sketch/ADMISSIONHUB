package com.admissionhub.dao;

import com.admissionhub.model.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    public int addStudentDocument(int studentId, String docType, String filePath) throws SQLException {
        String sql = "INSERT INTO documents (student_id, doc_type, file_path, verified) " +
                "VALUES (?, ?, ?, FALSE) RETURNING document_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, docType);
            ps.setString(3, filePath);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public boolean updateDocumentFile(int documentId, String filePath) throws SQLException {
        String sql = "UPDATE documents SET file_path=?, verified=FALSE WHERE document_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, filePath);
            ps.setInt(2, documentId);
            return ps.executeUpdate() > 0;
        }
    }

    public void upsertStudentDocument(int studentId, String docType, String filePath) throws SQLException {
        Document existing = getStudentDocumentByType(studentId, docType);
        if (existing != null) {
            updateDocumentFile(existing.getDocumentId(), filePath);
        } else {
            addStudentDocument(studentId, docType, filePath);
        }
    }

    public List<Document> getDocumentsByStudent(int studentId) throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE student_id = ? ORDER BY document_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Document getStudentDocumentByType(int studentId, String docType) throws SQLException {
        String sql = "SELECT * FROM documents WHERE student_id = ? AND doc_type = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, docType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Document> getAllDocuments() throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY document_id";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Document getDocumentById(int id) throws SQLException {
        String sql = "SELECT * FROM documents WHERE document_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public boolean updateDocument(Document d) throws SQLException {
        String sql = "UPDATE documents SET doc_type=?, file_path=?, verified=? WHERE document_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getDocType());
            ps.setString(2, d.getFilePath());
            ps.setBoolean(3, d.isVerified());
            ps.setInt(4, d.getDocumentId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteDocument(int id) throws SQLException {
        String sql = "DELETE FROM documents WHERE document_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Document mapRow(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setDocumentId(rs.getInt("document_id"));

        int studentId = rs.getInt("student_id");
        d.setStudentId(rs.wasNull() ? null : studentId);

        int applicationId = rs.getInt("application_id");
        d.setApplicationId(rs.wasNull() ? null : applicationId);

        d.setDocType(rs.getString("doc_type"));
        d.setFilePath(rs.getString("file_path"));
        Timestamp uploaded = rs.getTimestamp("uploaded_on");
        if (uploaded != null) d.setUploadedOn(uploaded.toLocalDateTime());
        d.setVerified(rs.getBoolean("verified"));
        return d;
    }
}