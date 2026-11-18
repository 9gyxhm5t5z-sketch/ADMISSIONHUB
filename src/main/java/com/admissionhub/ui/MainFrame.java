package com.admissionhub.ui;

import com.admissionhub.model.User;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame(User loggedInUser) {
        setTitle("AdmissionHub - Logged in as " + loggedInUser.getUsername()
                + " (" + loggedInUser.getRole() + ")");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        if (loggedInUser.isAdmin()) {
            tabbedPane.addTab("Dashboard", new DashboardPanel());
            tabbedPane.addTab("Students", new StudentPanel(loggedInUser));
            tabbedPane.addTab("Applications", new ApplicationPanel(loggedInUser));
            tabbedPane.addTab("Documents", new DocumentPanel(loggedInUser));
            tabbedPane.addTab("Edit Requests", new EditRequestsPanel());
        } else {
            tabbedPane.addTab("Dashboard", new StudentDashboardPanel(loggedInUser));
        }

        add(tabbedPane, BorderLayout.CENTER);
    }
}