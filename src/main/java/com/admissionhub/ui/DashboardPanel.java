package com.admissionhub.ui;

import com.admissionhub.dao.DashboardDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class DashboardPanel extends JPanel {

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    private JLabel totalStudentsValue, totalApplicationsValue, pendingValue, underReviewValue,
            approvedValue, rejectedValue, totalDocumentsValue, unverifiedDocsValue;

    public DashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Admissions Overview");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        add(title, BorderLayout.NORTH);

        add(buildStatsGrid(), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Stats");
        refreshBtn.addActionListener(e -> loadStats());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(refreshBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadStats();
    }

    private JPanel buildStatsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 4, 15, 15));

        totalStudentsValue = new JLabel("-", SwingConstants.CENTER);
        totalApplicationsValue = new JLabel("-", SwingConstants.CENTER);
        pendingValue = new JLabel("-", SwingConstants.CENTER);
        underReviewValue = new JLabel("-", SwingConstants.CENTER);
        approvedValue = new JLabel("-", SwingConstants.CENTER);
        rejectedValue = new JLabel("-", SwingConstants.CENTER);
        totalDocumentsValue = new JLabel("-", SwingConstants.CENTER);
        unverifiedDocsValue = new JLabel("-", SwingConstants.CENTER);

        grid.add(buildCard("Total Students", totalStudentsValue, new Color(52, 120, 246)));
        grid.add(buildCard("Total Applications", totalApplicationsValue, new Color(90, 90, 90)));
        grid.add(buildCard("Pending", pendingValue, new Color(230, 162, 60)));
        grid.add(buildCard("Under Review", underReviewValue, new Color(120, 120, 220)));
        grid.add(buildCard("Approved", approvedValue, new Color(60, 179, 113)));
        grid.add(buildCard("Rejected", rejectedValue, new Color(214, 69, 69)));
        grid.add(buildCard("Total Documents", totalDocumentsValue, new Color(90, 90, 90)));
        grid.add(buildCard("Unverified Docs", unverifiedDocsValue, new Color(230, 126, 34)));

        return grid;
    }

    private JPanel buildCard(String label, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2, true),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)));
        card.setBackground(Color.WHITE);

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 28f));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(label, SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 13f));

        card.add(valueLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(titleLabel);
        return card;
    }

    private void loadStats() {
        try {
            totalStudentsValue.setText(String.valueOf(dashboardDAO.getTotalStudents()));
            totalApplicationsValue.setText(String.valueOf(dashboardDAO.getTotalApplications()));
            pendingValue.setText(String.valueOf(dashboardDAO.getApplicationCountByStatus("PENDING")));
            underReviewValue.setText(String.valueOf(dashboardDAO.getApplicationCountByStatus("UNDER_REVIEW")));
            approvedValue.setText(String.valueOf(dashboardDAO.getApplicationCountByStatus("APPROVED")));
            rejectedValue.setText(String.valueOf(dashboardDAO.getApplicationCountByStatus("REJECTED")));
            totalDocumentsValue.setText(String.valueOf(dashboardDAO.getTotalDocuments()));
            unverifiedDocsValue.setText(String.valueOf(dashboardDAO.getUnverifiedDocuments()));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load dashboard stats: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}