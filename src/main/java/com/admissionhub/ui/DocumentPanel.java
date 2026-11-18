package com.admissionhub.ui;

import com.admissionhub.dao.DocumentDAO;
import com.admissionhub.dao.StudentDAO;
import com.admissionhub.model.Document;
import com.admissionhub.model.Student;
import com.admissionhub.model.User;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class DocumentPanel extends JPanel {

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final User currentUser;

    private JList<StudentItem> studentList;
    private DefaultListModel<StudentItem> studentListModel;

    private JLabel selectedStudentLabel;
    private JTable docsTable;
    private DefaultTableModel docsTableModel;

    private static final String[] DOC_TYPES = {
            "PROFILE_PICTURE", "DEGREE_10_CERTIFICATE", "MARKSHEET_11", "MARKSHEET_12",
            "DEGREE_12_CERTIFICATE", "OTHER_MARKSHEET", "PASSPORT_FIRST_PAGE", "VISA_PHOTO"
    };

    public DocumentPanel(User currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildStudentListPanel(), BorderLayout.WEST);
        add(buildDocumentsPanel(), BorderLayout.CENTER);

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

    private JPanel buildDocumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Documents"));

        selectedStudentLabel = new JLabel("Select a student on the left to view their documents.");
        selectedStudentLabel.setFont(selectedStudentLabel.getFont().deriveFont(Font.BOLD, 14f));
        selectedStudentLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(selectedStudentLabel, BorderLayout.NORTH);

        String[] columns = {"Document Type", "Status", "Uploaded On", "File"};
        docsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        docsTable = new JTable(docsTableModel);
        docsTable.setRowHeight(24);
        panel.add(new JScrollPane(docsTable), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton openBtn = new JButton("Open Selected File");
        openBtn.addActionListener(e -> onOpenFile());

        JButton verifyBtn = new JButton("Mark as Verified");
        verifyBtn.addActionListener(e -> onToggleVerified(true));

        JButton unverifyBtn = new JButton("Mark as Unverified");
        unverifyBtn.addActionListener(e -> onToggleVerified(false));

        JButton uploadBtn = new JButton("Upload Document for This Student");
        uploadBtn.addActionListener(e -> onUploadForStudent());

        btnRow.add(openBtn);
        btnRow.add(verifyBtn);
        btnRow.add(unverifyBtn);
        btnRow.add(uploadBtn);
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
        selectedStudentLabel.setText("Documents for: " + item.label);
        loadDocumentsForStudent(item.id);
    }

    private void loadDocumentsForStudent(int studentId) {
        docsTableModel.setRowCount(0);
        try {
            List<Document> docs = documentDAO.getDocumentsByStudent(studentId);
            for (Document d : docs) {
                docsTableModel.addRow(new Object[]{
                        d.getDocType(),
                        d.isVerified() ? "Verified" : "Pending Review",
                        d.getUploadedOn(),
                        d.getFilePath()
                });
            }
            if (docs.isEmpty()) {
                docsTableModel.addRow(new Object[]{"No documents uploaded yet.", "", "", ""});
            }
        } catch (SQLException ex) {
            showError("Failed to load documents: " + ex.getMessage());
        }
    }

    private void onOpenFile() {
        int row = docsTable.getSelectedRow();
        if (row == -1) {
            showError("Select a document row first.");
            return;
        }
        String path = String.valueOf(docsTableModel.getValueAt(row, 3));
        if (path.isEmpty() || path.equals("null")) {
            showError("No file available for this row.");
            return;
        }
        try {
            File file = new File(path);
            if (!file.exists()) {
                showError("File not found on disk: " + path);
                return;
            }
            Desktop.getDesktop().open(file);
        } catch (Exception ex) {
            showError("Could not open file: " + ex.getMessage());
        }
    }

    private void onToggleVerified(boolean verified) {
        int row = docsTable.getSelectedRow();
        if (row == -1) {
            showError("Select a document row first.");
            return;
        }
        StudentItem selectedStudent = studentList.getSelectedValue();
        if (selectedStudent == null) return;

        String docType = String.valueOf(docsTableModel.getValueAt(row, 0));
        try {
            Document doc = documentDAO.getStudentDocumentByType(selectedStudent.id, docType);
            if (doc == null) {
                showError("Could not find this document record.");
                return;
            }
            doc.setVerified(verified);
            documentDAO.updateDocument(doc);
            loadDocumentsForStudent(selectedStudent.id);
        } catch (SQLException ex) {
            showError("Failed to update status: " + ex.getMessage());
        }
    }

    private void onUploadForStudent() {
        StudentItem selected = studentList.getSelectedValue();
        if (selected == null) {
            showError("Select a student from the list first.");
            return;
        }

        String docType = (String) JOptionPane.showInputDialog(this,
                "Select document type to upload for " + selected.label + ":",
                "Upload Document", JOptionPane.PLAIN_MESSAGE, null, DOC_TYPES, DOC_TYPES[0]);
        if (docType == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select file for " + docType);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "PDF and Images (*.pdf, *.jpg, *.jpeg, *.png)", "pdf", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();
        try {
            String storedPath = copyFileToUploads(selectedFile.getAbsolutePath(), selected.id, docType);
            documentDAO.upsertStudentDocument(selected.id, docType, storedPath);
            loadDocumentsForStudent(selected.id);
            JOptionPane.showMessageDialog(this, "Document uploaded successfully.");
        } catch (Exception ex) {
            showError("Failed to upload: " + ex.getMessage());
        }
    }

    private String copyFileToUploads(String sourcePath, int studentId, String docType) throws IOException {
        Path uploadDirPath = Paths.get("uploads");
        if (!Files.exists(uploadDirPath)) Files.createDirectories(uploadDirPath);

        Path source = Paths.get(sourcePath);
        String originalName = source.getFileName().toString();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) extension = originalName.substring(dotIndex);

        String uniqueName = studentId + "_" + docType + "_" + UUID.randomUUID() + extension;
        Path destination = uploadDirPath.resolve(uniqueName);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}