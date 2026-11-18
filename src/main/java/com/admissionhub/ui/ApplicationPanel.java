package com.admissionhub.ui;

import com.admissionhub.dao.ApplicationDAO;
import com.admissionhub.dao.StudentDAO;
import com.admissionhub.model.AdmissionStatus;
import com.admissionhub.model.Application;
import com.admissionhub.model.Student;
import com.admissionhub.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class ApplicationPanel extends JPanel {

    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final User currentUser;

    private JList<StudentItem> studentList;
    private DefaultListModel<StudentItem> studentListModel;

    private JLabel selectedStudentLabel;
    private JTable appsTable;
    private DefaultTableModel appsTableModel;
    private JComboBox<AdmissionStatus> statusCombo;

    public ApplicationPanel(User currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildStudentListPanel(), BorderLayout.WEST);
        add(buildApplicationsPanel(), BorderLayout.CENTER);

        loadStudents();
    }

    private static class StudentItem {
        int id;
        String label;
        StudentItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private JPanel buildStudentListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Students"));

        studentListModel = new DefaultListModel<>();
        studentList = new JList<>(studentListModel);
        studentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && studentList.getSelectedValue() != null) {
                onStudentSelected(studentList.getSelectedValue());
            }
        });

        JButton refreshBtn = new JButton("Refresh Students");
        refreshBtn.addActionListener(e -> loadStudents());

        panel.add(new JScrollPane(studentList), BorderLayout.CENTER);
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildApplicationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Applications"));

        selectedStudentLabel = new JLabel("Select a student on the left to view their application(s).");
        selectedStudentLabel.setFont(selectedStudentLabel.getFont().deriveFont(Font.BOLD, 14f));
        selectedStudentLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(selectedStudentLabel, BorderLayout.NORTH);

        statusCombo = new JComboBox<>(AdmissionStatus.values());

        String[] columns = {"Application ID", "Course/Program", "Status", "Applied On"};
        appsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        appsTable = new JTable(appsTableModel);
        appsTable.setRowHeight(24);
        appsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appsTable.getSelectedRow() != -1) {
                Object status = appsTableModel.getValueAt(appsTable.getSelectedRow(), 2);
                try {
                    statusCombo.setSelectedItem(AdmissionStatus.valueOf(String.valueOf(status)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        });
        panel.add(new JScrollPane(appsTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton decideBtn = new JButton("Set Status");
        decideBtn.addActionListener(e -> onDecide());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            StudentItem selected = studentList.getSelectedValue();
            if (selected != null) loadApplicationsForStudent(selected.id);
        });

        btnRow.add(new JLabel("Status:"));
        btnRow.add(statusCombo);
        btnRow.add(decideBtn);
        btnRow.add(refreshBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private void loadStudents() {
        studentListModel.clear();
        try {
            List<Student> students = studentDAO.getAllStudents();
            for (Student s : students) {
                studentListModel.addElement(new StudentItem(s.getStudentId(),
                        s.getFullName() + " (ID " + s.getStudentId() + ")"));
            }
        } catch (SQLException ex) {
            showError("Failed to load students: " + ex.getMessage());
        }
    }

    private void onStudentSelected(StudentItem item) {
        selectedStudentLabel.setText("Application(s) for: " + item.label);
        loadApplicationsForStudent(item.id);
    }

    private void loadApplicationsForStudent(int studentId) {
        appsTableModel.setRowCount(0);
        try {
            List<Application> apps = applicationDAO.getApplicationsByStudent(studentId);
            for (Application a : apps) {
                appsTableModel.addRow(new Object[]{
                        a.getApplicationId(), a.getProgram(), a.getStatus(), a.getAppliedOn()
                });
            }
            if (apps.isEmpty()) {
                appsTableModel.addRow(new Object[]{"", "No application submitted yet.", "", ""});
            }
        } catch (SQLException ex) {
            showError("Failed to load applications: " + ex.getMessage());
        }
    }

    private void onDecide() {
        int row = appsTable.getSelectedRow();
        if (row == -1) {
            showError("Select an application row first.");
            return;
        }
        Object idValue = appsTableModel.getValueAt(row, 0);
        if (idValue == null || idValue.toString().isEmpty()) {
            showError("This row has no application to update.");
            return;
        }
        int applicationId = (int) idValue;
        AdmissionStatus newStatus = (AdmissionStatus) statusCombo.getSelectedItem();

        try {
            applicationDAO.updateStatus(applicationId, newStatus);
            StudentItem selected = studentList.getSelectedValue();
            if (selected != null) loadApplicationsForStudent(selected.id);
            JOptionPane.showMessageDialog(this, "Application status updated to: " + newStatus);
        } catch (SQLException ex) {
            showError("Failed to update status: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}