package com.javamini.timetable;

/**
 * Initializes and maps the overarching dataset of StudentGroups and Teachers.
 * Acts as the centralized state manager for populating entities dynamically from the DB prior to generation.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssignTeacher {

    public static StudentGroup[] studentgroup;
    public static Teacher[] teacher;
    public static int nostudentgroup, noteacher;
    public static int hoursperday, daysperweek;
    DatabaseConnection db = new DatabaseConnection();

    public AssignTeacher() {
        studentgroup = new StudentGroup[100];
        teacher = new Teacher[100];
    }

    /**
     * Initializes the dataset by loading all available StudentGroups and their allocated SubHrs.
     * Maps subject load and assigns pre-existing teachers directly from the Database constraints natively.
     */
    public void takeinput(int GrpId) {

        ResultSet rs = db.executeQuery("SELECT * FROM StudentGroup");
        int i = 0, j;
        try {
            while (rs.next()) {
                studentgroup[i] = new StudentGroup();
                studentgroup[i].id = rs.getInt("GrpId");
                studentgroup[i].name = rs.getString("GrpName");

                // Load actual subjects from SubHrs (not relying on NoOfSub which may be stale)
                DatabaseConnection subDb = new DatabaseConnection();
                ResultSet subHrs = subDb.executeQuery("SELECT * FROM SubHrs WHERE GrpId = " + studentgroup[i].id);
                j = 0;
                while (subHrs.next()) {
                    studentgroup[i].subjectid[j] = subHrs.getString("SubId");
                    studentgroup[i].hours[j] = subHrs.getInt("Hours");
                    // Load teacher if already assigned from a previous run
                    int tid = subHrs.getInt("TeacherId");
                    if (!subHrs.wasNull() && tid != 0) {
                        studentgroup[i].teacherid[j] = tid;
                    }
                    j++;
                }
                studentgroup[i].nosubject = j; // Use actual count, not NoOfSub
                subHrs.close();
                System.out.println("Loaded group: " + studentgroup[i].name + " with " + j + " subjects");
                i++;
                nostudentgroup = i;
            }
        } catch (SQLException ex) {
            Logger.getLogger(AssignTeacher.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(AssignTeacher.class.getName()).log(Level.SEVERE, null, ex);
        }

        rs = db.executeQuery("SELECT * FROM Teacher");
        i = 0;
        try {
            while (rs.next()) {
                teacher[i] = new Teacher();
                teacher[i].subject = new ArrayList<>();
                teacher[i].id = rs.getInt("TeacherId");
                teacher[i].name = rs.getString("TeacherName");
                ResultSet subHrs = new DatabaseConnection()
                        .executeQuery("SELECT * FROM TeacherSubject WHERE TeacherId = " + teacher[i].id);

                while (subHrs.next()) {
                    try {
                        teacher[i].subject.add(subHrs.getString("SubId"));
                    } catch (NullPointerException e) {
                    }

                }

                subHrs.close();
                i++;
                noteacher = i;
            }
        } catch (SQLException ex) {
        }

        try {
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(AssignTeacher.class.getName()).log(Level.SEVERE, null, ex);
        }

        assignTeacher(GrpId);
    }

    /**
     * Engine heuristic for auto-assigning teachers to unassigned core subjects for a target group natively.
     * Maps available faculties ensuring no faculty collision occurs against strict assignment paradigms.
     */
    public void assignTeacher(int GrpId) {
        // looping through every studentgroup
        for (int i = 0; i < nostudentgroup; i++) {
            // ** If the student froup is found
            if (studentgroup[i].id == GrpId) {
                // looping through every subjectid of a student group
                ResultSet rs = db.executeQuery("SELECT TeacherId FROM TimeTable WHERE GrpId = " + GrpId);
                try {
                    while (rs.next()) {
                        if (Teacher.getAssigned(rs.getInt("TeacherId")) != 0)
                            db.executeUpdate(
                                    "UPDATE Teacher SET assign = " + (Teacher.getAssigned(rs.getInt("TeacherId")) - 1)
                                            + " WHERE TeacherId = " + rs.getInt("TeacherId"));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(AssignTeacher.class.getName()).log(Level.SEVERE, null, ex);
                }
                for (int j = 0; j < studentgroup[i].nosubject; j++) {
                    int teacherid;
                    int assignedmin = -1;
                    String subject = studentgroup[i].subjectid[j];
                    if (subject == null) continue;

                    // OE subjects: skip teacher assignment (faculty from external dept)
                    if (isSubjectOE(subject)) {
                        studentgroup[i].teacherid[j] = -1;
                        db.executeUpdate("UPDATE SubHrs SET TeacherId = -1 WHERE GrpId = "
                                + studentgroup[i].id + " AND SubId='" + subject + "'");
                        System.out.println("OE subject '" + subject + "' - no teacher allocation needed");
                        continue;
                    }

                    // MANUAL ASSIGNMENT OVERRIDE
                    if (studentgroup[i].teacherid[j] > 0) {
                        System.out.println("Subject '" + subject + "' already manually assigned to teacher " + studentgroup[i].teacherid[j]);
                        continue;
                    }

                    // looping through every teacher to find which teacher teaches the current
                    // subjectid
                    for (int k = 0; k < noteacher; k++) {

                        for (int l = 0; l < teacher[k].subject.size(); l++) {
                            // if such teacher found, checking if he should be assigned the subjectid or some
                            // other teacher based on prior assignments

                            if (teacher[k].subject.get(l).equalsIgnoreCase(subject)) {
                                
                                // NEW LOGIC: Unique IT Faculty Check
                                boolean skipTeacher = false;
                                String currentGrpName = studentgroup[i].getName();
                                if ("ITA3".equals(currentGrpName) || "ITB3".equals(currentGrpName) || "ITC3".equals(currentGrpName)) {
                                    DatabaseConnection checkDb = new DatabaseConnection();
                                    ResultSet checkRs = checkDb.executeQuery(
                                        "SELECT COUNT(*) as conflicts FROM SubHrs sh " +
                                        "JOIN StudentGroup sg ON sh.GrpId = sg.GrpId " +
                                        "WHERE sh.TeacherId = " + teacher[k].id + " " +
                                        "AND sh.SubId = '" + subject + "' " +
                                        "AND sg.GrpName IN ('ITA3', 'ITB3', 'ITC3') " +
                                        "AND sg.GrpId != " + studentgroup[i].id
                                    );
                                    try {
                                        if (checkRs != null && checkRs.next() && checkRs.getInt("conflicts") > 0) {
                                            skipTeacher = true; 
                                        }
                                    } catch(SQLException ex) {}
                                }
                                if (skipTeacher) continue;

                                // if first teacher found for this subjectid
                                if (assignedmin == -1) {
                                    assignedmin = Teacher.getAssigned(teacher[k].id);
                                    teacherid = k;
                                    teacher[teacherid].assigned = Teacher.getAssigned(teacher[k].id) + 1;
                                    studentgroup[i].teacherid[j] = teacher[k].id;
                                    db.executeUpdate("UPDATE SubHrs SET TeacherId = '" + teacher[k].id
                                            + "' WHERE GrpId = " + studentgroup[i].id + " AND SubId='" + subject + "'");
                                    teacher[teacherid].setAssigned();
                                }

                                // if teacher found has less no of pre assignments than the teacher assigned for
                                // this subjectid
                                else if (assignedmin > teacher[k].assigned) {
                                    assignedmin = Teacher.getAssigned(teacher[k].id);
                                    teacherid = k;
                                    teacher[teacherid].assigned = Teacher.getAssigned(teacher[k].id) + 1;
                                    studentgroup[i].teacherid[j] = teacher[k].id;
                                    db.executeUpdate("UPDATE SubHrs SET TeacherId = '" + teacher[k].id
                                            + "' WHERE GrpId = " + studentgroup[i].id + " AND SubId='" + subject + "'");
                                    teacher[teacherid].setAssigned();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** Check if a subject is an Open Elective (OE) */
    private boolean isSubjectOE(String subId) {
        if (subId == null) return false;
        DatabaseConnection oeDb = new DatabaseConnection();
        ResultSet rs = oeDb.executeQuery("SELECT isOE FROM Subject WHERE SubId = '" + subId + "'");
        try {
            if (rs.next()) return rs.getBoolean("isOE");
        } catch (SQLException ex) { /* column may not exist yet */ }
        return false;
    }
}
