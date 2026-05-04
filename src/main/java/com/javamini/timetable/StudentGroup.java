package com.javamini.timetable;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entity tracking class groups (e.g. IT2A) and their assigned subject portfolios.
 * Features integrated database handlers for mapping arrays of subject data up to the 20 limits constraint natively.
 */
public class StudentGroup {
    public int id;
    public String name;
    public String[] subjectid;
    public int nosubject;
    public int teacherid[];
    public int[] hours;
    static DatabaseConnection db = new DatabaseConnection();

    public StudentGroup() {
        subjectid = new String[20];
        hours = new int[20];
        teacherid = new int[20];
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String[] getSubjectid() {
        return subjectid;
    }
    public void setSubjectid(String[] subjectid) {
        this.subjectid = subjectid;
    }
    public int getNosubject() {
        return nosubject;
    }
    public void setNosubject(String snosubject) {
        int nosubject=Integer.parseInt(snosubject);
        this.nosubject = nosubject;
    }
    public int[] getTeacherid() {
        return teacherid;
    }
    public void setTeacherid(int[] teacherid) {
        this.teacherid = teacherid;
    }
    public int[] getHours() {
        return hours;
    }
    public void setHours(int[] hours) {
        this.hours = hours;
    }
    
    //database handler
    
    public void addToRecord(String GrpName, int noOfSub){
        db.executeUpdate("INSERT INTO StudentGroup(GrpName,NoOfSub) VALUES('"+GrpName+"',"+noOfSub+")");
    }
    
    public void updateNoOfSubject(int id, int noOfSub){
        db.executeUpdate("UPDATE StudentGroup SET NoOfSub = "+noOfSub+" WHERE GrpId = "+id);
    }
    
    public void deleteRecord(int id){
        db.executeUpdate("DELETE FROM StudentGroup WHERE GrpId = "+id);
    }
    
    static int getId(String Name){
        ResultSet rs = db.executeQuery("SELECT * FROM StudentGroup WHERE GrpName = '"+Name+"'");
        try {
            if(rs.next()){
                return rs.getInt("GrpId");
            }
        } catch (SQLException ex) {
            Logger.getLogger(StudentGroup.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -2;
    }
    
    
}
