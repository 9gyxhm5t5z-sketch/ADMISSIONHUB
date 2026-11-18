package com.admissionhub.ui;

import com.admissionhub.dao.EditRequestDAO;
import com.admissionhub.model.EditRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class EditRequestsPanel extends JPanel {

    private final EditRequestDAO editRequestDAO = new EditRequestDAO();

    private JTable table;
    private DefaultTableModel tableModel;

    public EditRequestsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Pending Edit Requests");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.SOUTH);

        loadRequests();
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"Request ID", "Student ID", "Student Name", "Section", "Requested At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }

    private JPanel buildButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton approveBtn = new JButton("Approve");
        JButton rejectBtn = new JButton("Reject");
        JButton refreshBtn = new JButton("Refresh");

        approveBtn.addActionListener(e -> handleDecision(true));
        rejectBtn.addActionListener(e -> handleDecision(false));
        refreshBtn.addActionListener(e -> loadRequests());

        panel.add(approveBtn);
        panel.add(rejectBtn);
        panel.add(refreshBtn);
        return panel;
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        try {
            List<EditRequest> requests = editRequestDAO.getAllPending();
            for (EditRequest r : requests) {
                tableModel.addRow(new Object[]{
                        r.getRequestId(), r.getStudentId(), r.getStudentName(),
                        r.getSection(), r.getRequestedAt()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load requests: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDecision(boolean approve) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a request from the table first.");
            return;
        }
        int requestId = (int) tableModel.getValueAt(row, 0);
        try {
            if (approve) {
                editRequestDAO.approve(requestId);
                JOptionPane.showMessageDialog(this, "Request approved. The student can now edit that section once.");
            } else {
                editRequestDAO.reject(requestId);
                JOptionPane.showMessageDialog(this, "Request rejected.");
            }
            loadRequests();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}