package com.admissionhub.main;

import com.admissionhub.dao.PassportDetailsDAO;
import com.admissionhub.dao.StudentDAO;
import com.admissionhub.dao.VisaDetailsDAO;
import com.admissionhub.model.PassportDetails;
import com.admissionhub.model.Student;
import com.admissionhub.model.User;
import com.admissionhub.model.VisaDetails;
import com.admissionhub.ui.*;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            User loggedInUser = loginDialog.getAuthenticatedUser();
            if (loggedInUser == null) {
                System.exit(0);
            }

            if (!loggedInUser.isAdmin()) {
                try {
                    StudentDAO studentDAO = new StudentDAO();
                    PassportDetailsDAO passportDAO = new PassportDetailsDAO();
                    VisaDetailsDAO visaDAO = new VisaDetailsDAO();
                    int studentId = loggedInUser.getStudentId();

                    Student student = studentDAO.getStudentById(studentId);
                    if (student != null && !student.isProfileCompleted()) {
                        new CompleteProfileDialog(null, studentId).setVisible(true);
                    }

                    if (!passportDAO.existsForStudent(studentId)) {
                        new PassportDetailsDialog(null, studentId).setVisible(true);
                    }

                    if (!visaDAO.existsForStudent(studentId)) {
                        new VisaDetailsDialog(null, studentId).setVisible(true);
                    }

                    PassportDetails passport = passportDAO.getByStudentId(studentId);
                    if (passport != null && passport.isSkipped()) {
                        int choice = JOptionPane.showConfirmDialog(null,
                                "You haven't filled in your Passport Details yet.\nWould you like to fill them in now?",
                                "Reminder: Passport Details Missing",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice == JOptionPane.YES_OPTION) {
                            new PassportDetailsDialog(null, studentId).setVisible(true);
                        }
                    }

                    VisaDetails visa = visaDAO.getByStudentId(studentId);
                    if (visa != null && visa.isSkipped()) {
                        int choice = JOptionPane.showConfirmDialog(null,
                                "You haven't filled in your Visa Details yet.\nWould you like to fill them in now?",
                                "Reminder: Visa Details Missing",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (choice == JOptionPane.YES_OPTION) {
                            new VisaDetailsDialog(null, studentId).setVisible(true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            MainFrame frame = new MainFrame(loggedInUser);
            frame.setVisible(true);
        });
    }
}