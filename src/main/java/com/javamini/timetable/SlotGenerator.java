package com.javamini.timetable;

/**
 * Generates slot units representing a particular class session for a subject and teacher.
 * Calculates available playtime across the grid and dynamically injects required subject slots 
 * or open free periods based on the group's capacity rules.
 */
public class SlotGenerator {

    public static Slot[] slot;

    SlotGenerator(int GrpId) {

        DatabaseConnection db = new DatabaseConnection();
        // db.executeUpdate("DELETE FROM Slots WHERE GrpId = "+GrpId);

        int k = 0;
        int subjectno = 0;
        int hourcount = 1;
        int days = AssignTeacher.daysperweek;
        int hours = AssignTeacher.hoursperday;
        int nostgrp = AssignTeacher.nostudentgroup;

        // creating as many slots as the no of blocks in overall timetable
        slot = new Slot[hours * days * nostgrp];

        // looping for every student group
        for (int i = 0; i < nostgrp; i++) {

            subjectno = 0;
            // for every AVAILABLE slot in a week for a student group
            int hr = 0;
            if (AssignTeacher.studentgroup[i].id == GrpId) {
                
                String grpName = AssignTeacher.studentgroup[i].name;
                boolean isIT = "ITB3".equals(grpName) || "ITA3".equals(grpName) || "ITC3".equals(grpName);
                
                // Locked cells: 6 lunch + 1 Thursday Sports + Tech Skills
                int lockedSlots = days + 1; 
                if (grpName != null && grpName.contains("3")) {
                    lockedSlots += (hours - 1); // Friday full day (excluding lunch)
                }
                int availableSlots = hours * days - lockedSlots;

                for (int j = 0; j < availableSlots; j++) {

                    StudentGroup sg = AssignTeacher.studentgroup[i];

                    // if all subjects have been assigned required hours we give
                    // free periods

                    if (subjectno >= sg.nosubject) {
                        hr++;
                        slot[k++] = new Slot(sg, -1, "FR-0000");
                        // db.executeUpdate("INSERT INTO Slots(SlotId, GrpId, SubId, TeacherId)
                        // VALUES("+(k-1)+","+sg.id+",'FR-0000',-1)");

                        // slot[k++] = new Slot(sg, sg.teacherid[subjectno], sg.subjectid[subjectno]);
                    }

                    // if not we create new slots
                    else {

                        slot[k++] = new Slot(sg, sg.teacherid[subjectno], sg.subjectid[subjectno]);
                        // db.executeUpdate("INSERT INTO Slots(SlotId, GrpId, SubId, TeacherId)
                        // VALUES("+(k-1)+","+sg.id+",'"+sg.subjectid[subjectno]+"',"+sg.teacherid[subjectno]+")");
                        // suppose java has to be taught for 5 hours then we make 5
                        // slots for java, we keep track through hourcount
                        if (hourcount < sg.hours[subjectno]) {
                            hourcount++;
                        } else {
                            hourcount = 1;
                            subjectno++;
                        }
                    }
                }
            }
        }
    }

    public static Slot[] returnSlots() {
        return slot;
    }
}
