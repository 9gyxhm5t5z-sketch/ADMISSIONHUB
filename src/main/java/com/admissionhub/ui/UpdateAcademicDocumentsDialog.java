package com.admissionhub.ui;

import com.admissionhub.dao.DocumentDAO;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class UpdateAcademicDocumentsDialog extends JDialog {

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final int studentId;
    private static final String UPLOAD_DIR = "uploads";

    private static final LinkedHashMap<String, String> DOC_TYPES = new LinkedHashMap<>();
    static {
        DOC_TYPES.put("PROFILE_PICTURE", "Profile Picture");
        DOC_TYPES.put("DEGREE_10_CERTIFICATE", "10th Degree Certificate");
        DOC_TYPES.put("MARKSHEET_11", "11th Degree Mark Sheet");
        DOC_TYPES.put("MARKSHEET_12", "12th Degree Mark Sheet");
        DOC_TYPES.put("DEGREE_12_CERTIFICATE", "12th Degree Certificate");
        DOC_TYPES.put("OTHER_MARKSHEET", "Other Mark Sheets");
    }

    private final Map<String, String> pendingFiles = new LinkedHashMap<>();

    public UpdateAcademicDocumentsDialog(Frame owner, int studentId) {
        super(owner, "Update Documents", true);
        this.studentId = studentId;
        setSize(500, 400);
        setLocationRelativeTo(owner);
        buildUI();
    }

    private void buildUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel infoLabel = new JLabel("Select new files to replace your existing documents.");
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(infoLabel);
        panel.add(Box.createVerticalStrut(10));

        for (Map.Entry<String, String> entry : DOC_TYPES.entrySet()) {
            String docKey = entry.getKey();

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel nameLabel = new JLabel(entry.getValue());
            nameLabel.setPreferredSize(new Dimension(180, 20));

            JLabel statusLabel = new JLabel("No new file selected");
            statusLabel.setForeground(Color.GRAY);

            JButton browseBtn = new JButton("Browse");
            browseBtn.addActionListener(e -> onBrowse(docKey, statusLabel));

            row.add(nameLabel, BorderLayout.WEST);
            row.add(statusLabel, BorderLayout.CENTER);
            row.add(browseBtn, BorderLayout.EAST);
            panel.add(row);
        }

        panel.add(Box.createVerticalStrut(15));
        JButton saveBtn = new JButton("Save Changes");
        saveBtn.addActionListener(e -> onSave());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveBtn);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(btnPanel);

        setContentPane(new JScrollPane(panel));
    }

    private void onBrowse(String docKey, JLabel statusLabel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "PDF and Images (*.pdf, *.jpg, *.jpeg, *.png)", "pdf", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            pendingFiles.put(docKey, selected.getAbsolutePath());
            statusLabel.setText("Selected: " + selected.getName());
            statusLabel.setForeground(new Color(60, 130, 60));
        }
    }

    private String copyFileToUploads(String sourcePath, int studentId, String docType) throws IOException {
        Path uploadDirPath = Paths.get(UPLOAD_DIR);
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

    private void onSave() {
        if (pendingFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No new files selected.");
            return;
        }
        try {
            for (Map.Entry<String, String> entry : pendingFiles.entrySet()) {
                String storedPath = copyFileToUploads(entry.getValue(), studentId, entry.getKey());
                documentDAO.upsertStudentDocument(studentId, entry.getKey(), storedPath);
            }
            JOptionPane.showMessageDialog(this, "Documents updated successfully.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}