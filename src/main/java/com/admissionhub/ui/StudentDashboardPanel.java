package com.admissionhub.ui;

import com.admissionhub.dao.ApplicationDAO;
import com.admissionhub.dao.DocumentDAO;
import com.admissionhub.dao.EditRequestDAO;
import com.admissionhub.dao.PassportDetailsDAO;
import com.admissionhub.dao.StudentDAO;
import com.admissionhub.dao.VisaDetailsDAO;
import com.admissionhub.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class StudentDashboardPanel extends JPanel {

    private final StudentDAO studentDAO = new StudentDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final PassportDetailsDAO passportDAO = new PassportDetailsDAO();
    private final VisaDetailsDAO visaDAO = new VisaDetailsDAO();
    private final EditRequestDAO editRequestDAO = new EditRequestDAO();
    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final User currentUser;

    private JLabel profilePicLabel;
    private JLabel nameHeaderLabel;
    private DefaultTableModel docsTableModel;

    private JButton passportFillBtn;
    private JButton visaFillBtn;

    private JButton detailsUpdateBtn;
    private JButton passportUpdateBtn;
    private JButton visaUpdateBtn;
    private JButton documentsUpdateBtn;

    private JButton documentsRequestBtn;

    private Student cachedStudent;
    private PassportDetails cachedPassport;
    private VisaDetails cachedVisa;
    private String cachedStatus = "-";

    public StudentDashboardPanel(User currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(buildHeaderPanel(), BorderLayout.NORTH);
        add(buildDocumentsPanel(), BorderLayout.CENTER);

        loadData();
        showPendingNotifications();
    }

    private JPanel buildHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 10));

        JPanel topRow = new JPanel(new BorderLayout(15, 0));
        profilePicLabel = new JLabel();
        profilePicLabel.setPreferredSize(new Dimension(90, 90));
        profilePicLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topRow.add(profilePicLabel, BorderLayout.WEST);

        nameHeaderLabel = new JLabel();
        nameHeaderLabel.setFont(nameHeaderLabel.getFont().deriveFont(Font.BOLD, 18f));
        topRow.add(nameHeaderLabel, BorderLayout.CENTER);

        header.add(topRow, BorderLayout.NORTH);

        JPanel infoButtonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JButton myDetailsBtn = new JButton("My Details");
        myDetailsBtn.addActionListener(e -> showMyDetailsPopup());
        infoButtonsRow.add(myDetailsBtn);

        JButton visaDetailsBtn = new JButton("Visa Details");
        visaDetailsBtn.addActionListener(e -> showVisaDetailsPopup());
        infoButtonsRow.add(visaDetailsBtn);

        JButton passportDetailsBtn = new JButton("Passport Details");
        passportDetailsBtn.addActionListener(e -> showPassportDetailsPopup());
        infoButtonsRow.add(passportDetailsBtn);

        passportFillBtn = new JButton("Fill Passport Details");
        passportFillBtn.addActionListener(e -> openPassportDialog());
        infoButtonsRow.add(passportFillBtn);

        visaFillBtn = new JButton("Fill Visa Details");
        visaFillBtn.addActionListener(e -> openVisaDialog());
        infoButtonsRow.add(visaFillBtn);

        detailsUpdateBtn = greenButton("Update My Details", "DETAILS");
        infoButtonsRow.add(detailsUpdateBtn);

        passportUpdateBtn = greenButton("Update Passport Details", "PASSPORT");
        infoButtonsRow.add(passportUpdateBtn);

        visaUpdateBtn = greenButton("Update Visa Details", "VISA");
        infoButtonsRow.add(visaUpdateBtn);

        documentsUpdateBtn = greenButton("Update Documents", "DOCUMENTS");
        infoButtonsRow.add(documentsUpdateBtn);

        header.add(infoButtonsRow, BorderLayout.SOUTH);
        return header;
    }

    private JButton greenButton(String text, String section) {
        JButton btn = new JButton(text);
        btn.setBackground(new Color(60, 130, 60));
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> useApprovedPermission(section));
        return btn;
    }

    private JPanel buildDocumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("My Uploaded Documents"));

        String[] columns = {"Document Type", "Status", "File"};
        docsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable docsTable = new JTable(docsTableModel);
        docsTable.setRowHeight(24);

        JButton openBtn = new JButton("Open Selected File");
        openBtn.addActionListener(e -> {
            int row = docsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a document row first.");
                return;
            }
            String path = String.valueOf(docsTableModel.getValueAt(row, 2));
            if (path.equals("null") || path.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No file uploaded for this document.");
                return;
            }
            try {
                Desktop.getDesktop().open(new File(path));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open file: " + ex.getMessage());
            }
        });

        documentsRequestBtn = new JButton("Request to Update Documents");
        documentsRequestBtn.addActionListener(e -> requestPermission("DOCUMENTS"));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        panel.add(new JScrollPane(docsTable), BorderLayout.CENTER);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.add(openBtn);
        btnRow.add(documentsRequestBtn);
        btnRow.add(refreshBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    private void showMyDetailsPopup() {
        if (cachedStudent == null) return;
        String info = "Name: " + cachedStudent.getFullName() + "\n" +
                "Email: " + cachedStudent.getEmail() + "\n" +
                "Gender: " + orDash(cachedStudent.getGender()) + "\n" +
                "Date of Birth: " + (cachedStudent.getDob() != null ? cachedStudent.getDob().toString() : "-") + "\n" +
                "Place of Birth: " + orDash(cachedStudent.getPlaceOfBirth()) + "\n" +
                "Country of Birth: " + orDash(cachedStudent.getCountryOfBirth()) + "\n" +
                "Address: " + orDash(cachedStudent.getAddress()) + "\n" +
                "Course Applying For: " + orDash(cachedStudent.getCourseApplying()) + "\n" +
                "Admission Status: " + cachedStatus + "\n" +
                "WhatsApp: " + orDash(cachedStudent.getWhatsappContact()) + "\n" +
                "Phone: " + orDash(cachedStudent.getPhone());

        Object[] options = {"Close", "Request to Update"};
        int choice = JOptionPane.showOptionDialog(this, info, "My Details",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 1) requestPermission("DETAILS");
    }

    private void showPassportDetailsPopup() {
        if (cachedPassport == null || cachedPassport.isSkipped()) {
            JOptionPane.showMessageDialog(this,
                    "You haven't filled in your passport details yet.",
                    "Passport Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String info = "Nationality: " + orDash(cachedPassport.getNationality()) + "\n" +
                "Passport Number: " + orDash(cachedPassport.getPassportNumber()) + "\n" +
                "Country of Issue: " + orDash(cachedPassport.getCountryOfIssue()) + "\n" +
                "Place of Issue: " + orDash(cachedPassport.getPlaceOfIssue()) + "\n" +
                "Date of Issue: " + (cachedPassport.getDateOfIssue() != null ? cachedPassport.getDateOfIssue().toString() : "-") + "\n" +
                "Expiry Date: " + (cachedPassport.getExpiryDate() != null ? cachedPassport.getExpiryDate().toString() : "-");

        Object[] options = {"Close", "Request to Update"};
        int choice = JOptionPane.showOptionDialog(this, info, "Passport Details",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 1) requestPermission("PASSPORT");
    }

    private void showVisaDetailsPopup() {
        if (cachedVisa == null || cachedVisa.isSkipped()) {
            JOptionPane.showMessageDialog(this,
                    "You haven't filled in your visa details yet.",
                    "Visa Details", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String info = "Visa Number: " + orDash(cachedVisa.getVisaNumber()) + "\n" +
                "Country of Issue: " + orDash(cachedVisa.getCountryOfIssue()) + "\n" +
                "Place of Issue: " + orDash(cachedVisa.getPlaceOfIssue()) + "\n" +
                "Date of Issue: " + (cachedVisa.getDateOfIssue() != null ? cachedVisa.getDateOfIssue().toString() : "-") + "\n" +
                "Expiry Date: " + (cachedVisa.getExpiryDate() != null ? cachedVisa.getExpiryDate().toString() : "-");

        Object[] options = {"Close", "Request to Update"};
        int choice = JOptionPane.showOptionDialog(this, info, "Visa Details",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice == 1) requestPermission("VISA");
    }

    private String orDash(String value) {
        return (value == null || value.isEmpty()) ? "-" : value;
    }

    private void requestPermission(String section) {
        try {
            if (editRequestDAO.getApprovedUnusedRequest(currentUser.getStudentId(), section) != null) {
                JOptionPane.showMessageDialog(this,
                        "You're already approved to update this section. Use the 'Update' button.",
                        "Already Approved", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (editRequestDAO.hasPendingRequest(currentUser.getStudentId(), section)) {
                JOptionPane.showMessageDialog(this,
                        "Your request for this section is still awaiting admin approval.",
                        "Request Pending", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Editing this section requires approval from the admin.\nSend a request now?",
                    "Request Permission to Edit", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                editRequestDAO.createRequest(currentUser.getStudentId(), section);
                JOptionPane.showMessageDialog(this,
                        "Your request has been sent. You'll be notified once the admin decides.",
                        "Request Sent", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to process request: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void useApprovedPermission(String section) {
        try {
            EditRequest approved = editRequestDAO.getApprovedUnusedRequest(currentUser.getStudentId(), section);
            if (approved == null) {
                JOptionPane.showMessageDialog(this, "No approved request found.");
                return;
            }
            switch (section) {
                case "DETAILS" -> openDetailsUpdateDialog();
                case "PASSPORT" -> openPassportDialog();
                case "VISA" -> openVisaDialog();
                case "DOCUMENTS" -> openDocumentsUpdateDialog();
            }
            editRequestDAO.markUsed(approved.getRequestId());
            loadData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showPendingNotifications() {
        try {
            List<EditRequest> decisions = editRequestDAO.getUndismissedDecisions(currentUser.getStudentId());
            for (EditRequest r : decisions) {
                String sectionLabel = switch (r.getSection()) {
                    case "DETAILS" -> "My Details";
                    case "PASSPORT" -> "Passport Details";
                    case "VISA" -> "Visa Details";
                    case "DOCUMENTS" -> "Documents";
                    default -> r.getSection();
                };
                if (r.getStatus().equals("APPROVED")) {
                    JOptionPane.showMessageDialog(this,
                            "Your request to update your " + sectionLabel + " has been APPROVED.\n" +
                                    "You can now make changes using the 'Update' button.",
                            "Request Approved", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Your request to update your " + sectionLabel + " has been REJECTED.",
                            "Request Rejected", JOptionPane.WARNING_MESSAGE);
                }
                editRequestDAO.markNotified(r.getRequestId());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void openDetailsUpdateDialog() {
        UpdatePersonalDetailsDialog dialog = new UpdatePersonalDetailsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), currentUser.getStudentId());
        dialog.setVisible(true);
        loadData();
    }

    private void openPassportDialog() {
        PassportDetailsDialog dialog = new PassportDetailsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), currentUser.getStudentId());
        dialog.setVisible(true);
        loadData();
    }

    private void openVisaDialog() {
        VisaDetailsDialog dialog = new VisaDetailsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), currentUser.getStudentId());
        dialog.setVisible(true);
        loadData();
    }

    private void openDocumentsUpdateDialog() {
        UpdateAcademicDocumentsDialog dialog = new UpdateAcademicDocumentsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), currentUser.getStudentId());
        dialog.setVisible(true);
        loadData();
    }

    private void loadData() {
        try {
            int id = currentUser.getStudentId();

            cachedStudent = studentDAO.getStudentById(id);
            if (cachedStudent != null) {
                nameHeaderLabel.setText(cachedStudent.getFullName());
            }

            cachedPassport = passportDAO.getByStudentId(id);
            cachedVisa = visaDAO.getByStudentId(id);

            List<Application> apps = applicationDAO.getApplicationsByStudent(id);
            cachedStatus = apps.isEmpty() ? "-" : apps.get(apps.size() - 1).getStatus().name();

            passportFillBtn.setVisible(cachedPassport == null || cachedPassport.isSkipped());
            visaFillBtn.setVisible(cachedVisa == null || cachedVisa.isSkipped());

            detailsUpdateBtn.setVisible(editRequestDAO.getApprovedUnusedRequest(id, "DETAILS") != null);
            passportUpdateBtn.setVisible(editRequestDAO.getApprovedUnusedRequest(id, "PASSPORT") != null);
            visaUpdateBtn.setVisible(editRequestDAO.getApprovedUnusedRequest(id, "VISA") != null);
            documentsUpdateBtn.setVisible(editRequestDAO.getApprovedUnusedRequest(id, "DOCUMENTS") != null);

            loadProfilePicture();
            loadDocumentsTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load dashboard: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProfilePicture() throws SQLException {
        Document photoDoc = documentDAO.getStudentDocumentByType(currentUser.getStudentId(), "PROFILE_PICTURE");
        if (photoDoc != null && photoDoc.getFilePath() != null) {
            File file = new File(photoDoc.getFilePath());
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(photoDoc.getFilePath());
                Image scaled = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                profilePicLabel.setIcon(new ImageIcon(makeRoundedImage(scaled)));
                return;
            }
        }
        profilePicLabel.setIcon(new ImageIcon(makePlaceholderCircle()));
    }

    private Image makeRoundedImage(Image src) {
        int size = 90;
        java.awt.image.BufferedImage output =
                new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return output;
    }

    private Image makePlaceholderCircle() {
        int size = 90;
        java.awt.image.BufferedImage output =
                new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(220, 220, 220));
        g2.fillOval(0, 0, size, size);
        g2.setColor(Color.GRAY);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
        FontMetrics fm = g2.getFontMetrics();
        String text = "No Photo";
        g2.drawString(text, (size - fm.stringWidth(text)) / 2, size / 2 + 5);
        g2.dispose();
        return output;
    }

    private void loadDocumentsTable() throws SQLException {
        docsTableModel.setRowCount(0);
        List<Document> docs = documentDAO.getDocumentsByStudent(currentUser.getStudentId());
        for (Document d : docs) {
            if ("PROFILE_PICTURE".equals(d.getDocType())) continue;
            docsTableModel.addRow(new Object[]{
                    d.getDocType(),
                    d.isVerified() ? "Verified" : "Uploaded (pending review)",
                    d.getFilePath()
            });
        }
    }
}