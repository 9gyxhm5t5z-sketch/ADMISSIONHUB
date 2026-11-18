package com.admissionhub.dao;

import com.admissionhub.model.PassportDetails;

import java.sql.*;

public class PassportDetailsDAO {

    public boolean existsForStudent(int studentId) throws SQLException {
        String sql = "SELECT 1 FROM passport_details WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeQuery().next();
        }
    }

    public PassportDetails getByStudentId(int studentId) throws SQLException {
        String sql = "SELECT * FROM passport_details WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public int save(PassportDetails p) throws SQLException {
        String sql = "INSERT INTO passport_details " +
                "(student_id, nationality, passport_number, country_of_issue, place_of_issue, " +
                "date_of_issue, expiry_date, photo_file_path, skipped) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING passport_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getStudentId());
            ps.setString(2, p.getNationality());
            ps.setString(3, p.getPassportNumber());
            ps.setString(4, p.getCountryOfIssue());
            ps.setString(5, p.getPlaceOfIssue());
            ps.setDate(6, p.getDateOfIssue() != null ? Date.valueOf(p.getDateOfIssue()) : null);
            ps.setDate(7, p.getExpiryDate() != null ? Date.valueOf(p.getExpiryDate()) : null);
            ps.setString(8, p.getPhotoFilePath());
            ps.setBoolean(9, p.isSkipped());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public int saveSkipped(int studentId) throws SQLException {
        if (existsForStudent(studentId)) return -1;
        PassportDetails p = new PassportDetails();
        p.setStudentId(studentId);
        p.setSkipped(true);
        return save(p);
    }

    public void saveOrUpdate(PassportDetails p) throws SQLException {
        if (existsForStudent(p.getStudentId())) {
            String sql = "UPDATE passport_details SET nationality=?, passport_number=?, country_of_issue=?, " +
                    "place_of_issue=?, date_of_issue=?, expiry_date=?, photo_file_path=COALESCE(?, photo_file_path), skipped=? " +
                    "WHERE student_id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, p.getNationality());
                ps.setString(2, p.getPassportNumber());
                ps.setString(3, p.getCountryOfIssue());
                ps.setString(4, p.getPlaceOfIssue());
                ps.setDate(5, p.getDateOfIssue() != null ? Date.valueOf(p.getDateOfIssue()) : null);
                ps.setDate(6, p.getExpiryDate() != null ? Date.valueOf(p.getExpiryDate()) : null);
                ps.setString(7, p.getPhotoFilePath());
                ps.setBoolean(8, p.isSkipped());
                ps.setInt(9, p.getStudentId());
                ps.executeUpdate();
            }
        } else {
            save(p);
        }
    }

    private PassportDetails mapRow(ResultSet rs) throws SQLException {
        PassportDetails p = new PassportDetails();
        p.setPassportId(rs.getInt("passport_id"));
        p.setStudentId(rs.getInt("student_id"));
        p.setNationality(rs.getString("nationality"));
        p.setPassportNumber(rs.getString("passport_number"));
        p.setCountryOfIssue(rs.getString("country_of_issue"));
        p.setPlaceOfIssue(rs.getString("place_of_issue"));
        Date doi = rs.getDate("date_of_issue");
        if (doi != null) p.setDateOfIssue(doi.toLocalDate());
        Date exp = rs.getDate("expiry_date");
        if (exp != null) p.setExpiryDate(exp.toLocalDate());
        p.setPhotoFilePath(rs.getString("photo_file_path"));
        p.setSkipped(rs.getBoolean("skipped"));
        return p;
    }
}