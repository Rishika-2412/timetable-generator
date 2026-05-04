/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.javamini.timetable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author prabodhmayekar
 */
public class TimeTablePlusPlus {
    public static void main(String[] args) {

        DatabaseConnection db = new DatabaseConnection();

        // TimeTableGenerator.printSlots();
        ArrayList<String> day = new ArrayList<>();
        day.add("Monday");
        day.add("Tuesday");
        day.add("Wenesday");
        day.add("Thursday");
        day.add("Friday");
        day.add("Saturday");

        ArrayList<String> hour = new ArrayList<>();
        hour.add("9:40-10:40");
        hour.add("10:40-11:40");
        hour.add("11:40-12:40");
        hour.add("12:40-1:20");
        hour.add("1:20-2:20");
        hour.add("2:20-3:20");
        hour.add("3:20-4:20");
        // TimeTableGenerator.generateTimeTable(5,day,hour);

        SplashScreen splash = new SplashScreen();
        splash.setVisible(true);
        MainMenu mainMenu = new MainMenu();
        splash.progressBar.setValue(10);
        ManageTeachers manageTeachers = new ManageTeachers();
        splash.progressBar.setValue(20);
        ManageGroups manageGroups = new ManageGroups();
        splash.progressBar.setValue(30);
        ManageSubjects manageSubjects = new ManageSubjects();
        splash.progressBar.setValue(40);
        ManageTimeTable manageTimeTable = new ManageTimeTable();
        splash.progressBar.setValue(50);
        GenerateTimeTable generateTimeTable = new GenerateTimeTable();
        splash.progressBar.setValue(60);
        ViewTimeTable viewTimeTable = new ViewTimeTable();
        splash.progressBar.setValue(70);

        mainMenu.man_group.setOpaque(true);
        mainMenu.man_sub.setOpaque(true);
        mainMenu.man_teacher.setOpaque(true);
        mainMenu.man_tt.setOpaque(true);
        manageTeachers.addButton.setOpaque(true);
        manageTeachers.deleteButton.setOpaque(true);
        manageTeachers.assignButton.setOpaque(true);
        manageGroups.addButton.setOpaque(true);
        manageGroups.deleteGroup.setOpaque(true);
        manageGroups.show.setOpaque(true);
        manageSubjects.addButton.setOpaque(true);
        manageSubjects.deleteButton.setOpaque(true);
        manageTimeTable.genTimeTable.setOpaque(true);
        manageTimeTable.viewTimeTable.setOpaque(true);
        generateTimeTable.addButton.setOpaque(true);
        generateTimeTable.auto.setOpaque(true);
        splash.progressBar.setValue(80);

        // back button configuration
        manageGroups.backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                manageGroups.dispose();
                manageGroups.initInput();
                manageSubjects.initInput();
                manageTeachers.initInput();
                manageTimeTable.initInput();
                mainMenu.setVisible(true);
            }
        });

        manageSubjects.backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                manageSubjects.dispose();
                manageGroups.initInput();
                manageSubjects.initInput();
                manageTeachers.initInput();
                manageTimeTable.initInput();
                mainMenu.setVisible(true);

            }
        });

        manageTimeTable.backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                manageTimeTable.dispose();
                manageGroups.initInput();
                manageSubjects.initInput();
                manageTeachers.initInput();
                manageTimeTable.initInput();
                mainMenu.setVisible(true);
            }
        });

        manageTeachers.backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                manageTeachers.dispose();
                manageGroups.initInput();
                manageSubjects.initInput();
                manageTeachers.initInput();
                manageTimeTable.initInput();
                mainMenu.setVisible(true);
            }
        });

        generateTimeTable.backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                generateTimeTable.dispose();
                manageGroups.initInput();
                manageSubjects.initInput();
                manageTeachers.initInput();
                manageTimeTable.initInput();
                manageTimeTable.setVisible(true);
            }
        });

        viewTimeTable.backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                viewTimeTable.dispose();
                manageGroups.initInput();
                manageSubjects.initInput();
                manageTeachers.initInput();
                manageTimeTable.initInput();
                manageTimeTable.setVisible(true);
            }
        });

        // MainMenu configuration
        mainMenu.man_group.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                mainMenu.dispose();
                manageGroups.setVisible(true);

            }
        });

        mainMenu.man_teacher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                mainMenu.dispose();
                manageTeachers.setVisible(true);
            }
        });

        mainMenu.man_sub.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                mainMenu.dispose();
                manageSubjects.setVisible(true);
            }
        });

        mainMenu.man_tt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.

                mainMenu.dispose();

                manageTimeTable.setVisible(true);

            }
        });

        /*
         * ___________________________________GIT :
         * PROBS0809___________________________________________
         */
        // ManageTeacher Configurations
        manageTeachers.addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.

                String teacherName = manageTeachers.addTeacherName.getText();
                System.out.println("Teacher Name: " + teacherName);
                if (teacherName.equals("") || teacherName.equals("Teacher Name")) {
                    JOptionPane.showMessageDialog(null, "Please enter name");
                } else {
                    Teacher.addToRecored(teacherName);
                    manageTeachers.addTeacherName.setText("Teacher Name");
                    JOptionPane.showMessageDialog(null, "Successfully Added To Record");
                    System.out.println("Successfully added");
                }
                manageTeachers.initInput();
            }
        });

        manageTeachers.assignButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                int teacherId = 0;
                String subjectId = "";
                String teacherName = manageTeachers.assignTeacherName
                        .getItemAt(manageTeachers.assignTeacherName.getSelectedIndex());
                String subjectName = manageTeachers.assignSubjectName
                        .getItemAt(manageTeachers.assignSubjectName.getSelectedIndex());
                if (teacherName.equals("SELECT ITEM") || subjectName.equals("SELECT ITEM")) {
                    JOptionPane.showMessageDialog(null, "Please select the options properly");
                } else {
                    StringTokenizer tn = new StringTokenizer(teacherName, " : ");
                    StringTokenizer sn = new StringTokenizer(subjectName, " : ");
                    if (tn.hasMoreTokens() && sn.hasMoreTokens()) {
                        teacherId = Integer.parseInt(tn.nextToken());
                        subjectId = sn.nextToken();
                        db.executeUpdate("INSERT INTO TeacherSubject(TeacherId,SubId) VALUES(" + teacherId + ",'"
                                + subjectId + "')");
                        JOptionPane.showMessageDialog(null, "Success :)");

                    } else {
                        System.out.println("Error Processing the teacher assignment");
                        JOptionPane.showMessageDialog(null, "Some unusual error occur :(");
                    }
                    manageTeachers.assignTeacherName.setSelectedIndex(0);
                    manageTeachers.assignSubjectName.setSelectedIndex(0);
                }
                manageTeachers.initInput();
            }
        });

        manageTeachers.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                int teacherId = 0;
                String teacherName = manageTeachers.deleteTeacher
                        .getItemAt(manageTeachers.deleteTeacher.getSelectedIndex());
                if (teacherName.equals("SELECT ITEM")) {
                    JOptionPane.showMessageDialog(null, "Select item properly");
                } else {
                    StringTokenizer tn = new StringTokenizer(teacherName, " : ");

                    if (tn.hasMoreTokens()) {
                        teacherId = Integer.parseInt(tn.nextToken());
                        db.executeUpdate("DELETE FROM Teacher WHERE TeacherId = " + teacherId);
                        JOptionPane.showMessageDialog(null, "Success :)");

                    } else {
                        System.out.println("Problem in deleting teacher");
                        JOptionPane.showMessageDialog(null, "Error occured :(");
                    }
                    manageTeachers.deleteTeacher.setSelectedIndex(0);

                }
                manageTeachers.initInput();
            }
        });
        /*
         * _______________________________________________________________________________________________________________________________________
         */
        // ManageGroup configurations
        manageGroups.addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // throw new UnsupportedOperationException("Not supported yet."); //To change
                    // body of generated methods, choose Tools | Templates.
                    String groupName = manageGroups.addGroupName.getText();
                    String noOfSubjects = manageGroups.noOfSubjects
                            .getItemAt(manageGroups.noOfSubjects.getSelectedIndex());
                    ResultSet rs = db.executeQuery("SELECT * FROM StudentGroup WHERE GrpName = '" + groupName + "'");
                    if (!rs.next()) {
                        if (groupName.equals("") || groupName.equals("Group Name")
                                || noOfSubjects.equals("Select no of subjects")) {
                            JOptionPane.showMessageDialog(null, "Problem in input");
                        } else {
                            db.executeUpdate("INSERT INTO StudentGroup(GrpName, NoOfSub) VALUES('" + groupName + "',"
                                    + noOfSubjects + ")");
                            JOptionPane.showMessageDialog(null, "Success :)");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Group name already exists...");
                    }
                    manageGroups.noOfSubjects.setSelectedIndex(0);
                    manageGroups.addGroupName.setText("Group Name");
                    manageGroups.initInput();
                } catch (SQLException ex) {
                    Logger.getLogger(TimeTablePlusPlus.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        manageGroups.deleteGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // throw new UnsupportedOperationException("Not supported yet."); //To change
                    // body of generated methods, choose Tools | Templates.
                    String groupName = manageGroups.deleteGroupName
                            .getItemAt(manageGroups.deleteGroupName.getSelectedIndex());
                    ResultSet rs = db.executeQuery("SELECT * FROM StudentGroup WHERE GrpName = '" + groupName + "'");
                    if (rs.next()) {
                        if (groupName.equals("Select Group Name")) {
                            JOptionPane.showMessageDialog(null, "Problem in input");
                        } else {
                            db.executeUpdate("DELETE FROM StudentGroup WHERE GrpName = '" + groupName + "'");
                            JOptionPane.showMessageDialog(null, "Success :)");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "No record found");
                    }

                    manageGroups.deleteGroupName.setSelectedIndex(0);
                    manageGroups.initInput();
                } catch (SQLException ex) {
                    Logger.getLogger(TimeTablePlusPlus.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        manageGroups.show.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                manageGroups.initSH();
                try {
                    int n = 0;
                    String groupName = manageGroups.ashGroup.getItemAt(manageGroups.ashGroup.getSelectedIndex());
                    if (groupName.equals("Select Group Name")) {
                        JOptionPane.showMessageDialog(null, "Please select a group first");
                        return;
                    }
                    ResultSet rs = db.executeQuery("SELECT * FROM StudentGroup WHERE GrpName = '" + groupName + "'");
                    if (rs.next()) {
                        n = rs.getInt("NoOfSub");
                        final int grpId = rs.getInt("GrpId");

                        // Check if subjects are already assigned for this group
                        DatabaseConnection checkDb = new DatabaseConnection();
                        ResultSet existCheck = checkDb.executeQuery(
                                "SELECT COUNT(*) as cnt FROM SubHrs WHERE GrpId = " + grpId);
                        boolean hasExisting = false;
                        if (existCheck != null && existCheck.next()) {
                            hasExisting = existCheck.getInt("cnt") > 0;
                        }

                        if (hasExisting) {
                            int choice = JOptionPane.showConfirmDialog(null,
                                    "Subjects already assigned for '" + groupName + "'.\nDo you want to re-assign?",
                                    "Re-assign Subjects", JOptionPane.YES_NO_OPTION);
                            if (choice != JOptionPane.YES_OPTION) {
                                return;
                            }
                            db.executeUpdate("DELETE FROM SubHrs WHERE GrpId = " + grpId);
                        }

                        for (int i = 0; i < n; i++) {
                            manageGroups.jp.get(i).setVisible(true);
                            final int j = i;
                            // Remove old listeners to prevent stacking on repeated Show clicks
                            for (java.awt.event.ActionListener al : manageGroups.add.get(i).getActionListeners()) {
                                manageGroups.add.get(i).removeActionListener(al);
                            }
                            manageGroups.add.get(i).addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    String sub = manageGroups.sub.get(j)
                                            .getItemAt(manageGroups.sub.get(j).getSelectedIndex());
                                    String hr = manageGroups.hr.get(j)
                                            .getItemAt(manageGroups.hr.get(j).getSelectedIndex());
                                    if (!sub.equals("Select Subject") && !hr.equals("Hours")) {
                                        String subId = Subject.getSubjectId(sub);
                                        if (!subId.equals("No_Name_Found")) {
                                            db.executeUpdate("INSERT INTO SubHrs(GrpId,SubId,Hours) VALUES("
                                                    + grpId + ",'" + subId + "'," + hr + ")");
                                            manageGroups.add.get(j).setVisible(false);
                                            JOptionPane.showMessageDialog(null,
                                                    sub + " assigned with " + hr + " hrs/week");
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(null, "Please select subject and hours");
                                    }
                                }
                            });
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(TimeTablePlusPlus.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        // ManageSubject
        manageSubjects.addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String SubName = manageSubjects.addSubName.getText();
                String SubId = manageSubjects.addSubID.getText();
                if (SubName.equals("") || SubId.equals("") || SubName.equals("Subject Name")
                        || SubId.equals("Subject ID")) {
                    JOptionPane.showMessageDialog(null, "Problem in input");
                } else {
                    int credits = 3;
                    boolean isLab = false, isOE = false;

                    try {
                        String creditStr = JOptionPane.showInputDialog(manageSubjects, "Enter Credits/No. of classes per week for this subject:", "3");
                        if (creditStr != null) credits = Integer.parseInt(creditStr.trim());
                    } catch (Exception ignored) {}

                    int labChoice = JOptionPane.showConfirmDialog(manageSubjects, "Is this subject a Lab?", "Lab Subject", JOptionPane.YES_NO_OPTION);
                    isLab = (labChoice == JOptionPane.YES_OPTION);

                    if (!isLab) { 
                        int oeChoice = JOptionPane.showConfirmDialog(manageSubjects, "Is this subject an Open Elective (OE)?", "Open Elective", JOptionPane.YES_NO_OPTION);
                        isOE = (oeChoice == JOptionPane.YES_OPTION);
                    }

                    db.executeUpdate("INSERT INTO Subject(SubName,SubId,credits,isLab,isOE) VALUES('" + SubName + "','" + SubId + "'," + credits + "," + isLab + "," + isOE + ")");
                    manageSubjects.initInput();
                    JOptionPane.showMessageDialog(null, "Success :)");
                }
            }
        });

        manageSubjects.deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String SubId = manageSubjects.deleteSubject.getItemAt(manageSubjects.deleteSubject.getSelectedIndex());
                if (SubId.equals("Select Subject")) {
                    JOptionPane.showMessageDialog(null, "Problem with input");
                } else {
                    SubId = Subject.getSubjectId(SubId);
                    db.executeUpdate("DELETE FROM Subject WHERE SubId = '" + SubId + "'");
                    manageSubjects.initInput();
                    JOptionPane.showMessageDialog(null, "Success :)");
                }

            }
        });

        // manageTimeTable

        manageTimeTable.genTimeTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Old Swing-based generation disabled — use the web UI (localhost:8080) instead.
                // The core scheduling engine is now invoked through TimetableService.
                JOptionPane.showMessageDialog(null,
                    "Please use the web interface at http://localhost:8080 to generate timetables.");
            }
        });

        manageTimeTable.viewTimeTable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // throw new UnsupportedOperationException("Not supported yet."); //To change
                // body of generated methods, choose Tools | Templates.
                String GrpName = manageTimeTable.groupName.getItemAt(manageTimeTable.groupName.getSelectedIndex());
                int GrpId = StudentGroup.getId(GrpName);
                if (GrpId != -2) {

                    viewTimeTable.GrpId = GrpId;
                    manageTimeTable.dispose();
                    viewTimeTable.initInput();
                    viewTimeTable.setVisible(true);
                    System.out.println(GrpId + " " + GrpName);
                }
            }
        });

        splash.progressBar.setValue(90);
        splash.setVisible(false);
        mainMenu.setVisible(true);
    }
}