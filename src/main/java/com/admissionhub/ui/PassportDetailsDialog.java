package com.admissionhub.ui;

import com.admissionhub.dao.DocumentDAO;
import com.admissionhub.dao.PassportDetailsDAO;
import com.admissionhub.model.PassportDetails;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

public class PassportDetailsDialog extends JDialog {

    private final PassportDetailsDAO passportDAO = new PassportDetailsDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final int studentId;
    private static final String UPLOAD_DIR = "uploads";

    private JTextField nationalityField, passportNumberField, countryOfIssueField,
            placeOfIssueField, dateOfIssueField, expiryDateField;
    private JLabel photoStatusLabel;
    private String pendingPhotoPath = null;

    public PassportDetailsDialog(Frame owner, int studentId) {
        super(owner, "Passport Details", true);
        this.studentId = studentId;
        setSize(440, 440);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        buildUI();
        prefill();
    }

    private void buildUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel infoLabel = new JLabel("<html>Enter your passport details, or skip if not available yet.</html>");
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(infoLabel, gbc);
        gbc.gridwidth = 1;

        nationalityField = new JTextField(18);
        passportNumberField = new JTextField(18);
        countryOfIssueField = new JTextField(18);
        placeOfIssueField = new JTextField(18);
        dateOfIssueField = new JTextField(18);
        expiryDateField = new JTextField(18);

        int row = 1;
        addRow(panel, gbc, row++, "Nationality:", nationalityField);
        addRow(panel, gbc, row++, "Passport Number:", passportNumberField);
        addRow(panel, gbc, row++, "Country of Issue:", countryOfIssueField);
        addRow(panel, gbc, row++, "Place of Issue:", placeOfIssueField);
        addRow(panel, gbc, row++, "Date of Issue (YYYY-MM-DD):", dateOfIssueField);
        addRow(panel, gbc, row++, "Expiry Date (YYYY-MM-DD):", expiryDateField);

        gbc.gridx = 0; gbc.gridy = row; panel.add(new JLabel("Passport First Page (photo):"), gbc);
        JPanel photoPanel = new JPanel(new BorderLayout(5, 0));
        photoStatusLabel = new JLabel("Not uploaded");
        photoStatusLabel.setForeground(Color.GRAY);
        JButton uploadBtn = new JButton("Upload");
        uploadBtn.addActionListener(e -> onBrowsePhoto());
        photoPanel.add(photoStatusLabel, BorderLayout.CENTER);
        photoPanel.add(uploadBtn, BorderLayout.EAST);
        gbc.gridx = 1; panel.add(photoPanel, gbc);
        row++;

        JButton saveBtn = new JButton("Save");
        JButton skipBtn = new JButton("Skip for Now");
        saveBtn.addActionListener(e -> onSave());
        skipBtn.addActionListener(e -> onSkip());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(skipBtn);
        btnPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        panel.add(btnPanel, gbc);

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
            PassportDetails existing = passportDAO.getByStudentId(studentId);
            if (existing != null && !existing.isSkipped()) {
                nationalityField.setText(existing.getNationality() != null ? existing.getNationality() : "");
                passportNumberField.setText(existing.getPassportNumber() != null ? existing.getPassportNumber() : "");
                countryOfIssueField.setText(existing.getCountryOfIssue() != null ? existing.getCountryOfIssue() : "");
                placeOfIssueField.setText(existing.getPlaceOfIssue() != null ? existing.getPlaceOfIssue() : "");
                if (existing.getDateOfIssue() != null) dateOfIssueField.setText(existing.getDateOfIssue().toString());
                if (existing.getExpiryDate() != null) expiryDateField.setText(existing.getExpiryDate().toString());
                if (existing.getPhotoFilePath() != null) {
                    photoStatusLabel.setText("Existing file on record (browse to replace)");
                    photoStatusLabel.setForeground(new Color(60, 130, 60));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBrowsePhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select passport first page photo");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Images and PDF (*.jpg, *.jpeg, *.png, *.pdf)", "jpg", "jpeg", "png", "pdf"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            pendingPhotoPath = selected.getAbsolutePath();
            photoStatusLabel.setText("Selected: " + selected.getName());
            photoStatusLabel.setForeground(new Color(60, 130, 60));
        }
    }

    private String copyFileToUploads(String sourcePath, int studentId, String tag) throws IOException {
        Path uploadDirPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadDirPath)) Files.createDirectories(uploadDirPath);

        Path source = Paths.get(sourcePath);
        String originalName = source.getFileName().toString();
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex >= 0) extension = originalName.substring(dotIndex);

        String uniqueName = studentId + "_" + tag + "_" + UUID.randomUUID() + extension;
        Path destination = uploadDirPath.resolve(uniqueName);
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }

    private void onSave() {
        try {
            String nationality = nationalityField.getText().trim();
            String passportNumber = passportNumberField.getText().trim();
            String countryOfIssue = countryOfIssueField.getText().trim();
            String placeOfIssue = placeOfIssueField.getText().trim();
            String dateOfIssueText = dateOfIssueField.getText().trim();
            String expiryDateText = expiryDateField.getText().trim();

            if (nationality.isEmpty() || passportNumber.isEmpty() || countryOfIssue.isEmpty()
                    || placeOfIssue.isEmpty() || dateOfIssueText.isEmpty() || expiryDateText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields, or click Skip if you don't have this yet.");
                return;
            }

            LocalDate dateOfIssue, expiryDate;
            try {
                dateOfIssue = LocalDate.parse(dateOfIssueText);
                expiryDate = LocalDate.parse(expiryDateText);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Dates must be in format YYYY-MM-DD.");
                return;
            }

            String storedPhotoPath = null;
            if (pendingPhotoPath != null) {
                storedPhotoPath = copyFileToUploads(pendingPhotoPath, studentId, "PASSPORT_FIRST_PAGE");
                documentDAO.upsertStudentDocument(studentId, "PASSPORT_FIRST_PAGE", storedPhotoPath);
            }

            PassportDetails p = new PassportDetails();
            p.setStudentId(studentId);
            p.setNationality(nationality);
            p.setPassportNumber(passportNumber);
            p.setCountryOfIssue(countryOfIssue);
            p.setPlaceOfIssue(placeOfIssue);
            p.setDateOfIssue(dateOfIssue);
            p.setExpiryDate(expiryDate);
            p.setPhotoFilePath(storedPhotoPath);
            p.setSkipped(false);

            passportDAO.saveOrUpdate(p);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to save: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSkip() {
        try {
            passportDAO.saveSkipped(studentId);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to skip: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}