package com.admissionhub.ui;

import com.admissionhub.dao.DocumentDAO;
import com.admissionhub.dao.StudentDAO;
import com.admissionhub.model.Student;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class CompleteProfileDialog extends JDialog {

    private final StudentDAO studentDAO = new StudentDAO();
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

    private JTextField firstNameField, lastNameField, dobField, placeOfBirthField,
            countryOfBirthField, addressField, courseApplyingField;

    private final Map<String, String> pendingFiles = new LinkedHashMap<>();

    public CompleteProfileDialog(Frame owner, int studentId) {
        super(owner, "Complete Your Profile", true);
        this.studentId = studentId;
        setSize(520, 680);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        buildUI();
        prefillNames();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel infoLabel = new JLabel("Please complete your personal details and upload your documents.");
        infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(infoLabel);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(buildPersonalDetailsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildDocumentsPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        JButton saveBtn = new JButton("Save and Continue");
        saveBtn.addActionListener(e -> onSave());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveBtn);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(btnPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);
    }

    private JPanel buildPersonalDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Personal Details"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        firstNameField = new JTextField(18);
        lastNameField = new JTextField(18);
        dobField = new JTextField(18);
        placeOfBirthField = new JTextField(18);
        countryOfBirthField = new JTextField(18);
        addressField = new JTextField(18);
        courseApplyingField = new JTextField(18);

        firstNameField.setEditable(false);
        lastNameField.setEditable(false);
        firstNameField.setBackground(new Color(240, 240, 240));
        lastNameField.setBackground(new Color(240, 240, 240));

        int row = 0;
        addRow(panel, gbc, row++, "First Name:", firstNameField);
        addRow(panel, gbc, row++, "Last Name:", lastNameField);
        addRow(panel, gbc, row++, "Date of Birth (YYYY-MM-DD):", dobField);
        addRow(panel, gbc, row++, "Place of Birth:", placeOfBirthField);
        addRow(panel, gbc, row++, "Country of Birth:", countryOfBirthField);
        addRow(panel, gbc, row++, "Address:", addressField);
        addRow(panel, gbc, row++, "Course Applying For:", courseApplyingField);

        return panel;
    }

    private JPanel buildDocumentsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Upload Documents"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Map.Entry<String, String> entry : DOC_TYPES.entrySet()) {
            String docKey = entry.getKey();
            String docLabel = entry.getValue();

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel nameLabel = new JLabel(docLabel);
            nameLabel.setPreferredSize(new Dimension(180, 20));

            JLabel statusLabel = new JLabel("Not uploaded");
            statusLabel.setForeground(Color.GRAY);

            JButton browseBtn = new JButton("Upload");
            browseBtn.addActionListener(e -> onBrowseDoc(docKey, statusLabel));

            row.add(nameLabel, BorderLayout.WEST);
            row.add(statusLabel, BorderLayout.CENTER);
            row.add(browseBtn, BorderLayout.EAST);
            panel.add(row);
        }

        return panel;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    private void prefillNames() {
        try {
            Student s = studentDAO.getStudentById(studentId);
            if (s != null) {
                firstNameField.setText(s.getFirstName());
                lastNameField.setText(s.getLastName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBrowseDoc(String docKey, JLabel statusLabel) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select file for " + DOC_TYPES.get(docKey));
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
        try {
            String dobText = dobField.getText().trim();
            String placeOfBirth = placeOfBirthField.getText().trim();
            String countryOfBirth = countryOfBirthField.getText().trim();
            String address = addressField.getText().trim();
            String courseApplying = courseApplyingField.getText().trim();

            if (dobText.isEmpty() || placeOfBirth.isEmpty() || countryOfBirth.isEmpty()
                    || address.isEmpty() || courseApplying.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill in all personal detail fields, including the course you're applying for.");
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

            for (Map.Entry<String, String> pending : pendingFiles.entrySet()) {
                String storedPath = copyFileToUploads(pending.getValue(), studentId, pending.getKey());
                documentDAO.upsertStudentDocument(studentId, pending.getKey(), storedPath);
            }

            JOptionPane.showMessageDialog(this, "Profile completed successfully!");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}