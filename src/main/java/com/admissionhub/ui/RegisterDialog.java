package com.admissionhub.ui;

import com.admissionhub.dao.StudentDAO;
import com.admissionhub.dao.UserDAO;
import com.admissionhub.model.Student;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();

    private JTextField firstNameField, lastNameField, emailField, whatsappField, phoneField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> genderCombo;

    public RegisterDialog(Frame owner) {
        super(owner, "Student Registration", true);
        setSize(380, 440);
        setLocationRelativeTo(owner);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);
        passwordField = new JPasswordField(15);
        confirmPasswordField = new JPasswordField(15);
        genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        whatsappField = new JTextField(15);
        phoneField = new JTextField(15);

        int row = 0;
        addRow(panel, gbc, row++, "First Name:", firstNameField);
        addRow(panel, gbc, row++, "Last Name:", lastNameField);
        addRow(panel, gbc, row++, "Email:", emailField);
        addRow(panel, gbc, row++, "Password:", passwordField);
        addRow(panel, gbc, row++, "Confirm Password:", confirmPasswordField);
        addRow(panel, gbc, row++, "Gender:", genderCombo);
        addRow(panel, gbc, row++, "WhatsApp Contact:", whatsappField);
        addRow(panel, gbc, row++, "Phone Contact:", phoneField);

        JButton registerBtn = new JButton("Continue");
        registerBtn.addActionListener(e -> onRegister());

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(registerBtn, gbc);

        setContentPane(panel);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void onRegister() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String gender = (String) genderCombo.getSelectedItem();
            String whatsapp = whatsappField.getText().trim();
            String phone = phoneField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                    || password.isEmpty() || whatsapp.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all required fields.");
                return;
            }
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.");
                return;
            }
            if (userDAO.usernameExists(email)) {
                JOptionPane.showMessageDialog(this, "An account with this email already exists.");
                return;
            }

            Student student = new Student(firstName, lastName, email, gender, whatsapp, phone);
            int studentId = studentDAO.addStudent(student);
            userDAO.createStudentUser(email, password, studentId);

            JOptionPane.showMessageDialog(this,
                    "Account created! Log in now to complete your personal details.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}