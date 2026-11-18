package com.admissionhub.ui;

import com.admissionhub.dao.UserDAO;
import com.admissionhub.model.User;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class LoginDialog extends JDialog {

    private final UserDAO userDAO = new UserDAO();
    private User authenticatedUser = null;

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginDialog(Frame owner) {
        super(owner, "AdmissionHub - Login", true);
        setSize(340, 190);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Username / Email:"), gbc);
        gbc.gridx = 1; panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);

        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register as Student");
        loginBtn.addActionListener(e -> onLogin());
        registerBtn.addActionListener(e -> onRegister());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginBtn);
        buttonPanel.add(registerBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        passwordField.addActionListener(e -> onLogin());
        setContentPane(panel);
    }

    private void onLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both username and password.");
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.authenticatedUser = user;
            dispose();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister() {
        dispose();
        RegisterDialog registerDialog = new RegisterDialog((Frame) getOwner());
        registerDialog.setVisible(true);
        LoginDialog loginAgain = new LoginDialog((Frame) getOwner());
        loginAgain.setVisible(true);
        this.authenticatedUser = loginAgain.getAuthenticatedUser();
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }
}