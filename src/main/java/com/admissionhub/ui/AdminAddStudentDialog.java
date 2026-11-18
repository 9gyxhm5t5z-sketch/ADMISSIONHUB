package com.admissionhub.ui;

import com.admissionhub.dao.DocumentDAO;
import com.admissionhub.dao.PassportDetailsDAO;
import com.admissionhub.dao.StudentDAO;
import com.admissionhub.dao.VisaDetailsDAO;
import com.admissionhub.model.PassportDetails;
import com.admissionhub.model.Student;
import com.admissionhub.model.VisaDetails;

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

public class AdminAddStudentDialog extends JDialog {

    private final StudentDAO studentDAO = new StudentDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PassportDetailsDAO passportDAO = new PassportDetailsDAO();
    private final VisaDetailsDAO visaDAO = new VisaDetailsDAO();
    private static final String UPLOAD_DIR = "uploads";

    private static final LinkedHashMap<String, String> DOC_TYPES = new LinkedHashMap<>();
    static {
        DOC_TYPES.put("PROFILE_PICTURE", "Profile Picture");
        DOC_TYPES.put("DEGREE_10_CERTIFICATE", "10th Degree Certificate");
        DOC_TYPES.put("MARKSHEET_11", "11th Degree Mark Sheet");
        DOC_TYPES.put("MARKSHEET_12", "12th Degree Mark Sheet");
        DOC_TYPES.put("DEGREE_12_CERTIFICATE", "12th Degree Certificate");
        DOC_TYPES.put("OTHER_MARKSHEET", "Other Mark Sheets");
        DOC_TYPES.put("PASSPORT_FIRST_PAGE", "Passport First Page");
        DOC_TYPES.put("VISA_PHOTO", "Visa Photo");
    }

    private JTextField firstNameField, lastNameField, emailField, whatsappField, phoneField;
    private JComboBox<String> genderCombo;
    private JTextField dobField, placeOfBirthField, countryOfBirthField, addressField, courseApplyingField;
    private JTextField nationalityField, passportNumberField, passportCountryField,
            passportPlaceField, passportIssueField, passportExpiryField;
    private JTextField visaNumberField, visaCountryField, visaPlaceField, visaIssueField, visaExpiryField;

    private final Map<String, String> pendingFiles = new LinkedHashMap<>();

    public AdminAddStudentDialog(Frame owner) {
        super(owner, "Add New Student - Full Profile", true);
        setSize(620, 700);
        setLocationRelativeTo(owner);
        setResizable(true);
        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(buildAccountPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildPersonalDetailsPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildPassportPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildVisaPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buildDocumentsPanel());
        mainPanel.add(Box.createVerticalStrut(15));

        JButton saveBtn = new JButton("Create Student");
        saveBtn.addActionListener(e -> onSave());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveBtn);
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(btnPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        setContentPane(scrollPane);
    }

    private GridBagConstraints newGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private JPanel buildAccountPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Account Info"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = newGbc();

        firstNameField = new JTextField(18);
        lastNameField = new JTextField(18);
        emailField = new JTextField(18);
        genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        whatsappField = new JTextField(18);
        phoneField = new JTextField(18);

        int row = 0;
        addRow(panel, gbc, row++, "First Name:", firstNameField);
        addRow(panel, gbc, row++, "Last Name:", lastNameField);
        addRow(panel, gbc, row++, "Email:", emailField);
        addRow(panel, gbc, row++, "Gender:", genderCombo);
        addRow(panel, gbc, row++, "WhatsApp Contact:", whatsappField);
        addRow(panel, gbc, row++, "Phone Contact:", phoneField);
        return panel;
    }

    private JPanel buildPersonalDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Personal Details"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = newGbc();

        dobField = new JTextField(18);
        placeOfBirthField = new JTextField(18);
        countryOfBirthField = new JTextField(18);
        addressField = new JTextField(18);
        courseApplyingField = new JTextField(18);

        int row = 0;
        addRow(panel, gbc, row++, "Date of Birth (YYYY-MM-DD):", dobField);
        addRow(panel, gbc, row++, "Place of Birth:", placeOfBirthField);
        addRow(panel, gbc, row++, "Country of Birth:", countryOfBirthField);
        addRow(panel, gbc, row++, "Address:", addressField);
        addRow(panel, gbc, row++, "Course Applying For:", courseApplyingField);
        return panel;
    }

    private JPanel buildPassportPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Passport Details (optional)"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = newGbc();

        nationalityField = new JTextField(18);
        passportNumberField = new JTextField(18);
        passportCountryField = new JTextField(18);
        passportPlaceField = new JTextField(18);
        passportIssueField = new JTextField(18);
        passportExpiryField = new JTextField(18);

        int row = 0;
        addRow(panel, gbc, row++, "Nationality:", nationalityField);
        addRow(panel, gbc, row++, "Passport Number:", passportNumberField);
        addRow(panel, gbc, row++, "Country of Issue:", passportCountryField);
        addRow(panel, gbc, row++, "Place of Issue:", passportPlaceField);
        addRow(panel, gbc, row++, "Date of Issue (YYYY-MM-DD):", passportIssueField);
        addRow(panel, gbc, row++, "Expiry Date (YYYY-MM-DD):", passportExpiryField);
        return panel;
    }

    private JPanel buildVisaPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Visa Details (optional)"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = newGbc();

        visaNumberField = new JTextField(18);
        visaCountryField = new JTextField(18);
        visaPlaceField = new JTextField(18);
        visaIssueField = new JTextField(18);
        visaExpiryField = new JTextField(18);

        int row = 0;
        addRow(panel, gbc, row++, "Visa Number:", visaNumberField);
        addRow(panel, gbc, row++, "Country of Issue:", visaCountryField);
        addRow(panel, gbc, row++, "Place of Issue:", visaPlaceField);
        addRow(panel, gbc, row++, "Date of Issue (YYYY-MM-DD):", visaIssueField);
        addRow(panel, gbc, row++, "Expiry Date (YYYY-MM-DD):", visaExpiryField);
        return panel;
    }

    private JPanel buildDocumentsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Upload Documents (optional)"));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Map.Entry<String, String> entry : DOC_TYPES.entrySet()) {
            String docKey = entry.getKey();

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

            JLabel nameLabel = new JLabel(entry.getValue());
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
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "First name, last name, and email are required.");
                return;
            }

            String dobText = dobField.getText().trim();
            LocalDate dob = null;
            if (!dobText.isEmpty()) {
                try {
                    dob = LocalDate.parse(dobText);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "DOB must be in format YYYY-MM-DD.");
                    return;
                }
            }

            Student student = new Student(firstName, lastName, email,
                    (String) genderCombo.getSelectedItem(),
                    whatsappField.getText().trim(), phoneField.getText().trim());
            int studentId = studentDAO.addStudent(student);

            studentDAO.completeProfile(studentId, dob,
                    placeOfBirthField.getText().trim(), countryOfBirthField.getText().trim(),
                    addressField.getText().trim(), courseApplyingField.getText().trim());

            String passportNumber = passportNumberField.getText().trim();
            if (!passportNumber.isEmpty()) {
                PassportDetails p = new PassportDetails();
                p.setStudentId(studentId);
                p.setNationality(nationalityField.getText().trim());
                p.setPassportNumber(passportNumber);
                p.setCountryOfIssue(passportCountryField.getText().trim());
                p.setPlaceOfIssue(passportPlaceField.getText().trim());
                p.setDateOfIssue(parseDateOrNull(passportIssueField.getText().trim()));
                p.setExpiryDate(parseDateOrNull(passportExpiryField.getText().trim()));
                p.setSkipped(false);
                passportDAO.save(p);
            }

            String visaNumber = visaNumberField.getText().trim();
            if (!visaNumber.isEmpty()) {
                VisaDetails v = new VisaDetails();
                v.setStudentId(studentId);
                v.setVisaNumber(visaNumber);
                v.setCountryOfIssue(visaCountryField.getText().trim());
                v.setPlaceOfIssue(visaPlaceField.getText().trim());
                v.setDateOfIssue(parseDateOrNull(visaIssueField.getText().trim()));
                v.setExpiryDate(parseDateOrNull(visaExpiryField.getText().trim()));
                v.setSkipped(false);
                visaDAO.save(v);
            }

            for (Map.Entry<String, String> entry : pendingFiles.entrySet()) {
                String storedPath = copyFileToUploads(entry.getValue(), studentId, entry.getKey());
                documentDAO.upsertStudentDocument(studentId, entry.getKey(), storedPath);
            }

            JOptionPane.showMessageDialog(this, "Student created successfully.");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to create student: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate parseDateOrNull(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }
}