package com.admissionhub.ui;

import com.admissionhub.dao.StudentDAO;
import com.admissionhub.model.Student;
import com.admissionhub.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StudentPanel extends JPanel {

    private final StudentDAO studentDAO = new StudentDAO();
    private final User currentUser;

    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField firstNameField, lastNameField, emailField, whatsappField, phoneField,
            dobField, placeOfBirthField, countryOfBirthField, addressField, courseField;
    private JComboBox<String> genderCombo;
    private int selectedStudentId = -1;

    public StudentPanel(User currentUser) {
        this.currentUser = currentUser;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadStudents();
    }

    private JPanel buildFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        firstNameField = new JTextField(12);
        lastNameField = new JTextField(12);
        emailField = new JTextField(15);
        genderCombo = new JComboBox<>(new String[]{"MALE", "FEMALE", "OTHER"});
        whatsappField = new JTextField(12);
        phoneField = new JTextField(12);
        dobField = new JTextField(10);
        placeOfBirthField = new JTextField(12);
        countryOfBirthField = new JTextField(12);
        addressField = new JTextField(15);
        courseField = new JTextField(15);

        addPair(formPanel, gbc, 0, 0, "First Name:", firstNameField);
        addPair(formPanel, gbc, 0, 2, "Last Name:", lastNameField);
        addPair(formPanel, gbc, 1, 0, "Email:", emailField);
        addPair(formPanel, gbc, 1, 2, "Gender:", genderCombo);
        addPair(formPanel, gbc, 2, 0, "WhatsApp:", whatsappField);
        addPair(formPanel, gbc, 2, 2, "Phone:", phoneField);
        addPair(formPanel, gbc, 3, 0, "DOB (YYYY-MM-DD):", dobField);
        addPair(formPanel, gbc, 3, 2, "Place of Birth:", placeOfBirthField);
        addPair(formPanel, gbc, 4, 0, "Country of Birth:", countryOfBirthField);
        addPair(formPanel, gbc, 4, 2, "Address:", addressField);
        addPair(formPanel, gbc, 5, 0, "Course:", courseField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton updateBtn = new JButton("Update");
        JButton clearBtn = new JButton("Clear");
        JButton refreshBtn = new JButton("Refresh");

        updateBtn.addActionListener(this::onUpdate);
        clearBtn.addActionListener(e -> clearForm());
        refreshBtn.addActionListener(e -> loadStudents());

        buttonPanel.add(updateBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(refreshBtn);

        if (currentUser.isAdmin()) {
            JButton deleteBtn = new JButton("Delete");
            deleteBtn.addActionListener(this::onDelete);
            buttonPanel.add(deleteBtn);

            JButton addFullBtn = new JButton("Add New Student (Full Profile)");
            addFullBtn.addActionListener(e -> openAdminAddStudentDialog());
            buttonPanel.add(addFullBtn);

            JButton passportBtn = new JButton("Passport Details");
            passportBtn.addActionListener(e -> openPassportForSelected());
            buttonPanel.add(passportBtn);

            JButton visaBtn = new JButton("Visa Details");
            visaBtn.addActionListener(e -> openVisaForSelected());
            buttonPanel.add(visaBtn);
        }

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        formPanel.add(buttonPanel, gbc);

        return formPanel;
    }

    private void addPair(JPanel panel, GridBagConstraints gbc, int row, int col, String label, JComponent field) {
        gbc.gridwidth = 1;
        gbc.gridy = row;
        gbc.gridx = col;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = col + 1;
        panel.add(field, gbc);
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"ID", "First Name", "Last Name", "Email", "Gender", "WhatsApp",
                "Phone", "DOB", "Place of Birth", "Country of Birth", "Address", "Course"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                populateFormFromSelectedRow();
            }
        });
        return new JScrollPane(table);
    }

    private String str(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private void populateFormFromSelectedRow() {
        int row = table.getSelectedRow();
        selectedStudentId = (int) tableModel.getValueAt(row, 0);
        firstNameField.setText(str(tableModel.getValueAt(row, 1)));
        lastNameField.setText(str(tableModel.getValueAt(row, 2)));
        emailField.setText(str(tableModel.getValueAt(row, 3)));
        genderCombo.setSelectedItem(str(tableModel.getValueAt(row, 4)));
        whatsappField.setText(str(tableModel.getValueAt(row, 5)));
        phoneField.setText(str(tableModel.getValueAt(row, 6)));
        dobField.setText(str(tableModel.getValueAt(row, 7)));
        placeOfBirthField.setText(str(tableModel.getValueAt(row, 8)));
        countryOfBirthField.setText(str(tableModel.getValueAt(row, 9)));
        addressField.setText(str(tableModel.getValueAt(row, 10)));
        courseField.setText(str(tableModel.getValueAt(row, 11)));
    }

    private void loadStudents() {
        tableModel.setRowCount(0);
        try {
            if (currentUser.isAdmin()) {
                List<Student> students = studentDAO.getAllStudents();
                for (Student s : students) addRowToTable(s);
            } else {
                Student s = studentDAO.getStudentById(currentUser.getStudentId());
                if (s != null) {
                    addRowToTable(s);
                    table.setRowSelectionInterval(0, 0);
                }
            }
        } catch (SQLException ex) {
            showError("Failed to load students: " + ex.getMessage());
        }
    }

    private void addRowToTable(Student s) {
        tableModel.addRow(new Object[]{
                s.getStudentId(), s.getFirstName(), s.getLastName(), s.getEmail(),
                s.getGender(), s.getWhatsappContact(), s.getPhone(), s.getDob(),
                s.getPlaceOfBirth(), s.getCountryOfBirth(), s.getAddress(), s.getCourseApplying()
        });
    }

    private void onUpdate(ActionEvent e) {
        if (selectedStudentId == -1) {
            showError("Select a student from the table first.");
            return;
        }
        if (!currentUser.isAdmin() && selectedStudentId != currentUser.getStudentId()) {
            showError("You can only update your own profile.");
            return;
        }
        try {
            Student s = buildStudentFromForm();
            s.setStudentId(selectedStudentId);
            boolean updated = studentDAO.updateStudent(s);
            loadStudents();
            clearForm();
            JOptionPane.showMessageDialog(this, updated ? "Student updated." : "No changes made.");
        } catch (Exception ex) {
            showError("Failed to update student: " + ex.getMessage());
        }
    }

    private void onDelete(ActionEvent e) {
        if (selectedStudentId == -1) {
            showError("Select a student from the table first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this student? This also deletes their applications, documents and login.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            studentDAO.deleteStudent(selectedStudentId);
            loadStudents();
            clearForm();
        } catch (SQLException ex) {
            showError("Failed to delete student: " + ex.getMessage());
        }
    }

    private void openAdminAddStudentDialog() {
        AdminAddStudentDialog dialog = new AdminAddStudentDialog(
                (Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        loadStudents();
    }

    private void openPassportForSelected() {
        if (selectedStudentId == -1) {
            showError("Select a student from the table first.");
            return;
        }
        PassportDetailsDialog dialog = new PassportDetailsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selectedStudentId);
        dialog.setVisible(true);
    }

    private void openVisaForSelected() {
        if (selectedStudentId == -1) {
            showError("Select a student from the table first.");
            return;
        }
        VisaDetailsDialog dialog = new VisaDetailsDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), selectedStudentId);
        dialog.setVisible(true);
    }

    private Student buildStudentFromForm() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String dobText = dobField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("First name, last name, and email are required.");
        }

        LocalDate dob = null;
        if (!dobText.isEmpty()) {
            try {
                dob = LocalDate.parse(dobText);
            } catch (Exception e) {
                throw new IllegalArgumentException("DOB must be in format YYYY-MM-DD.");
            }
        }

        Student s = new Student();
        s.setFirstName(firstName);
        s.setLastName(lastName);
        s.setEmail(email);
        s.setGender((String) genderCombo.getSelectedItem());
        s.setWhatsappContact(whatsappField.getText().trim());
        s.setPhone(phoneField.getText().trim());
        s.setDob(dob);
        s.setPlaceOfBirth(placeOfBirthField.getText().trim());
        s.setCountryOfBirth(countryOfBirthField.getText().trim());
        s.setAddress(addressField.getText().trim());
        s.setCourseApplying(courseField.getText().trim());
        return s;
    }

    private void clearForm() {
        selectedStudentId = -1;
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        genderCombo.setSelectedIndex(0);
        whatsappField.setText("");
        phoneField.setText("");
        dobField.setText("");
        placeOfBirthField.setText("");
        countryOfBirthField.setText("");
        addressField.setText("");
        courseField.setText("");
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}