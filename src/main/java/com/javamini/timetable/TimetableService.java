package com.javamini.timetable;

import java.util.ArrayList;

/**
 * Service layer that orchestrates timetable generation.
 * Wraps AssignTeacher, SlotGenerator, and TimeTableGenerator.
 */
public class TimetableService {

    private static final String[] DAY_NAMES = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String[] HOUR_LABELS = {
        "9:40-10:40", "10:40-11:40", "11:40-12:40", "12:40-1:20",
        "1:20-2:20", "2:20-3:20", "3:20-4:20"
    };

    /**
     * Generate a timetable for the given group.
     * Steps: load data -> assign teachers -> create slots -> run backtracking
     */
    public String generate(int grpId) {
        // 1. Set schedule dimensions
        AssignTeacher.hoursperday = 7; // includes lunch column
        AssignTeacher.daysperweek = 6;

        // 2. Load all student groups, teachers, and auto-assign from DB
        // Note: takeinput() internally calls assignTeacher(grpId)
        AssignTeacher at = new AssignTeacher();
        at.takeinput(grpId);

        // 4. Create slot distribution (29 schedulable slots)
        new SlotGenerator(grpId);

        // 5. Prepare day/hour lists
        ArrayList<String> day = new ArrayList<>();
        for (String d : DAY_NAMES) day.add(d);

        ArrayList<String> hour = new ArrayList<>();
        for (String h : HOUR_LABELS) hour.add(h);

        // 6. Run headless timetable generation (backtracking + pre-fills)
        return TimeTableGenerator.generateTimeTableHeadless(grpId, day, hour);
    }
}
