package com.admissionhub.ui;

import com.admissionhub.dao.StudentDAO;
import com.admissionhub.model.Student;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

public class UpdatePersonalDetailsDialog extends JDialog {

    private final StudentDAO studentDAO = new StudentDAO();
    private final int studentId;

    private JTextField dobField, placeOfBirthField, countryOfBirthField, addressField, courseApplyingField;

    public UpdatePersonalDetailsDialog(Frame owner, int studentId) {
        super(owner, "Update My Details", true);
        this.studentId = studentId;
        setSize(420, 320);
        setLocationRelativeTo(owner);
        buildUI();
        prefill();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        dobField = new JTextField(18);
        placeOfBirthField = new JTextField(18);
        countryOfBirthField = new JTextField(18);
        addressField = new JTextField(18);
        courseApplyingField = new JTextField(18);

        int row = 0;
        addRow(panel, gbc, row++, "Date of Birth (YYYY-MM-DD):", dobField);
        addRow(panel, gbc, row++, "Place of Birth:", placeOfBirthField);
        addRow(panel, gbc, row++, "Country of Birth:", countryOfBirthField);
        addRow(panel, gbc, row++, "Address:", addressField);
        addRow(panel, gbc, row++, "Course Applying For:", courseApplyingField);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> onSave());

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(saveBtn, gbc);

        setContentPane(panel);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void prefill() {
        try {
            Student s = studentDAO.getStudentById(studentId);
            if (s != null) {
                if (s.getDob() != null) dobField.setText(s.getDob().toString());
                placeOfBirthField.setText(s.getPlaceOfBirth() != null ? s.getPlaceOfBirth() : "");
                countryOfBirthField.setText(s.getCountryOfBirth() != null ? s.getCountryOfBirth() : "");
                addressField.setText(s.getAddress() != null ? s.getAddress() : "");
                courseApplyingField.setText(s.getCourseApplying() != null ? s.getCourseApplying() : "");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onSave() {
        try {
            String dobText = dobField.getText().trim();
            String placeOfBirth = placeOfBirthField.getText().trim();
            String countryOfBirth = countryOfBirthField.getText().trim();
            String address = addressField.getText().trim();
            String courseApplying = courseApplyingField.getText().trim();

            if (dobText.isEmpty() || placeOfBirth.isEmpty() || countryOfBirth.isEmpty()
                    || address.isEmpty() || courseApplying.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            LocalDate dob;
            try {
                dob = LocalDate.parse(dobText);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "DOB must be in format YYYY-MM-DD.");
                return;
            }

            studentDAO.completeProfile(studentId, dob, placeOfBirth, countryOfBirth, address, courseApplying);
            JOptionPane.showMessageDialog(this, "Details updated successfully.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}