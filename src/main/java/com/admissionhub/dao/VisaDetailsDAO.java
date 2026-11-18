package com.admissionhub.dao;

import com.admissionhub.model.VisaDetails;

import java.sql.*;

public class VisaDetailsDAO {

    public boolean existsForStudent(int studentId) throws SQLException {
        String sql = "SELECT 1 FROM visa_details WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeQuery().next();
        }
    }

    public VisaDetails getByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM visa_details WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public int save(VisaDetails v) throws SQLException {
        String sql = "INSERT INTO visa_details " +
                "(student_id, visa_number, country_of_issue, place_of_issue, " +
                "date_of_issue, expiry_date, photo_file_path, skipped) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING visa_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, v.getStudentId());
            ps.setString(2, v.getVisaNumber());
            ps.setString(3, v.getCountryOfIssue());
            ps.setString(4, v.getPlaceOfIssue());
            ps.setDate(5, v.getDateOfIssue() != null ? Date.valueOf(v.getDateOfIssue()) : null);
            ps.setDate(6, v.getExpiryDate() != null ? Date.valueOf(v.getExpiryDate()) : null);
            ps.setString(7, v.getPhotoFilePath());
            ps.setBoolean(8, v.isSkipped());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public int saveSkipped(int studentId) throws SQLException {
        if (existsForStudent(studentId)) return -1;
        VisaDetails v = new VisaDetails();
        v.setStudentId(studentId);
        v.setSkipped(true);
        return save(v);
    }

    public void saveOrUpdate(VisaDetails v) throws SQLException {
        if (existsForStudent(v.getStudentId())) {
            String sql = "UPDATE visa_details SET visa_number=?, country_of_issue=?, place_of_issue=?, " +
                    "date_of_issue=?, expiry_date=?, photo_file_path=COALESCE(?, photo_file_path), skipped=? WHERE student_id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, v.getVisaNumber());
                ps.setString(2, v.getCountryOfIssue());
                ps.setString(3, v.getPlaceOfIssue());
                ps.setDate(4, v.getDateOfIssue() != null ? Date.valueOf(v.getDateOfIssue()) : null);
                ps.setDate(5, v.getExpiryDate() != null ? Date.valueOf(v.getExpiryDate()) : null);
                ps.setString(6, v.getPhotoFilePath());
                ps.setBoolean(7, v.isSkipped());
                ps.setInt(8, v.getStudentId());
                ps.executeUpdate();
            }
        } else {
            save(v);
        }
    }

    private VisaDetails mapRow(ResultSet rs) throws SQLException {
        VisaDetails v = new VisaDetails();
        v.setVisaId(rs.getInt("visa_id"));
        v.setStudentId(rs.getInt("student_id"));
        v.setVisaNumber(rs.getString("visa_number"));
        v.setCountryOfIssue(rs.getString("country_of_issue"));
        v.setPlaceOfIssue(rs.getString("place_of_issue"));
        Date doi = rs.getDate("date_of_issue");
        if (doi != null) v.setDateOfIssue(doi.toLocalDate());
        Date exp = rs.getDate("expiry_date");
        if (exp != null) v.setExpiryDate(exp.toLocalDate());
        v.setPhotoFilePath(rs.getString("photo_file_path"));
        v.setSkipped(rs.getBoolean("skipped"));
        return v;
    }
}