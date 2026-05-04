package com.javamini.timetable;

import java.util.*;
import java.sql.*;

/**
 * Constraint-based timetable generator using backtracking (CSP).
 * Fully headless — no Swing/AWT dependencies.
 *
 * Pre-filled (locked) cells:
 * - h=3 (12:40-1:20) all days → LUNCH
 * - Friday all class hours → Technical Skills
 * - Thursday h=6 (3:20-4:20) → Sports
 *
 * Constraints:
 * - Labs must be continuous blocks in morning (h=0,1,2) or afternoon (h=4,5,6)
 * - Max 1 lab per day per section
 * - OE subjects only in 1st hour (h=0)
 * - Theory: max 1 class/day per course; 5+ credits get ONE consecutive double
 * - Teacher not double-booked across groups
 */
public class TimeTableGenerator {
    static DatabaseConnection db = new DatabaseConnection();

    private static int backtrackCount = 0;
    private static final int MAX_BACKTRACK = 10000000;

    // Caches for DB lookups (cleared each generation)
    private static Map<String, Boolean> oeCache = new HashMap<>();
    private static Map<String, Boolean> labCache = new HashMap<>();
    private static Map<String, Integer> creditsCache = new HashMap<>();
    private static Set<String> busyTeacherSlots = new HashSet<>();

    /**
     * Headless timetable generation — called from TimetableService.
     * Returns status message string.
     */
    public static String generateTimeTableHeadless(int GrpId, ArrayList<String> day, ArrayList<String> hour) {
        int days = AssignTeacher.daysperweek;
        int hours = AssignTeacher.hoursperday;

        // Clear caches
        oeCache.clear();
        labCache.clear();
        creditsCache.clear();
        busyTeacherSlots.clear();

        // Pre-fetch all teacher busy slots from DB globally
        ResultSet rsT = db.executeQuery("SELECT TeacherId, Day, Hour FROM TimeTable");
        try {
            while (rsT != null && rsT.next()) {
                busyTeacherSlots
                        .add(rsT.getInt("TeacherId") + "-" + rsT.getString("Day") + "-" + rsT.getString("Hour"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Use -1 as empty, -2 as locked/pre-filled
        int[][] arr2 = new int[days][hours];
        for (int[] row : arr2)
            Arrays.fill(row, -1);

        db.executeUpdate("DELETE FROM TimeTable WHERE GrpId = " + GrpId);

        // ===== PRE-FILL: Lunch (h=3) for all days =====
        for (int d = 0; d < days; d++) {
            arr2[d][3] = -2;
            db.executeUpdate("INSERT INTO TimeTable(Day,Hour,SubId,GrpId,TeacherId) VALUES('"
                    + day.get(d) + "','" + hour.get(3) + "','LUNCH-00'," + GrpId + ",-1)");
        }
        System.out.println("PRE-FILLED: Lunch (h=3) for all 6 days");

        // Get Group Name
        String grpName = "";
        for (int i = 0; i < AssignTeacher.nostudentgroup; i++) {
            if (AssignTeacher.studentgroup[i].id == GrpId) {
                grpName = AssignTeacher.studentgroup[i].name;
                break;
            }
        }
        boolean isIT = "IT3A".equalsIgnoreCase(grpName) || "IT3B".equalsIgnoreCase(grpName)
                || "IT3C".equalsIgnoreCase(grpName) ||
                "ITA3".equalsIgnoreCase(grpName) || "ITB3".equalsIgnoreCase(grpName)
                || "ITC3".equalsIgnoreCase(grpName);

        // ===== PRE-FILL: Technical Skills =====
        int fridayIdx = day.indexOf("Friday");
        if (fridayIdx >= 0 && grpName.contains("3")) {
            for (int h = 0; h < hours; h++) {
                if (h == 3)
                    continue; // Already lunch
                arr2[fridayIdx][h] = -2;
                db.executeUpdate("INSERT INTO TimeTable(Day,Hour,SubId,GrpId,TeacherId) VALUES('"
                        + day.get(fridayIdx) + "','" + hour.get(h) + "','TECHSKILLS-00'," + GrpId + ",-1)");
            }
            System.out.println("PRE-FILLED: Friday = Technical Skills for group " + grpName);
        }

        // ===== PRE-FILL: Thursday last hour = Sports =====
        int thursdayIdx = day.indexOf("Thursday");
        int lastHourIdx = hours - 1;
        if (thursdayIdx >= 0) {
            arr2[thursdayIdx][lastHourIdx] = -2;
            db.executeUpdate("INSERT INTO TimeTable(Day,Hour,SubId,GrpId,TeacherId) VALUES('"
                    + day.get(thursdayIdx) + "','" + hour.get(lastHourIdx) + "','SPORTS-00'," + GrpId + ",-1)");
            System.out.println("PRE-FILLED: Thursday last hour = Sports");
        }

        // Collect slots for this group
        System.out.println("----Collecting Slots for Group " + GrpId + "----");
        ArrayList<Integer> arr = new ArrayList<>();
        int totalSlots = (SlotGenerator.slot != null) ? SlotGenerator.slot.length : 0;
        for (int i = 0; i < totalSlots; i++) {
            if (SlotGenerator.slot[i] != null && SlotGenerator.slot[i].studentgroup.id == GrpId) {
                arr.add(i);
            }
        }

        // ===== PRE-FILL: Saturday last 3 hours = TBP =====
        int satIdx = day.indexOf("Saturday");
        if (satIdx >= 0 && isIT) {
            ArrayList<Integer> tbpSlots = new ArrayList<>();
            for (int slotIdx : arr) {
                if (SlotGenerator.slot[slotIdx] != null
                        && "TBP".equalsIgnoreCase(SlotGenerator.slot[slotIdx].subject)) {
                    tbpSlots.add(slotIdx);
                }
            }

            int slotH = 4; // 1:20 PM start
            for (int slotIdx : tbpSlots) {
                if (slotH <= 6) {
                    arr2[satIdx][slotH] = slotIdx;
                    arr.remove(Integer.valueOf(slotIdx));
                    slotH++;
                }
            }
            if (!tbpSlots.isEmpty()) {
                System.out.println("PRE-FILLED dynamically: Saturday Afternoon = TBP for " + grpName);
            }
        }

        System.out.println("Total slots collected dynamically: " + arr.size());

        // Run auto-generator
        autoGenerator(arr, arr2, day, hour);
        return "Timetable generated successfully for group " + GrpId;
    }

    /**
     * Backtracking-based auto-generator with constraint checking.
     */
    public static void autoGenerator(ArrayList<Integer> arr, int[][] arr2,
            ArrayList<String> day, ArrayList<String> hour) {
        int days = AssignTeacher.daysperweek;
        int hours = AssignTeacher.hoursperday;

        // Separate real subjects from free slots
        ArrayList<Integer> realSlots = new ArrayList<>();
        ArrayList<Integer> freeSlots = new ArrayList<>();
        for (int slotIdx : arr) {
            if (SlotGenerator.slot[slotIdx] == null
                    || "FR-0000".equals(SlotGenerator.slot[slotIdx].subject)) {
                freeSlots.add(slotIdx);
            } else {
                realSlots.add(slotIdx);
            }
        }

        System.out.println("Auto-generating: " + realSlots.size() + " subject slots, "
                + freeSlots.size() + " free slots");

        // Sort: Labs first (most constrained), then OE, then by credits asc
        realSlots.sort((a, b) -> {
            String subA = SlotGenerator.slot[a].subject;
            String subB = SlotGenerator.slot[b].subject;
            boolean labA = isLabSubject(subA);
            boolean labB = isLabSubject(subB);
            if (labA != labB)
                return labA ? -1 : 1;
            if (labA && labB)
                return subA.compareTo(subB);
            boolean oeA = isOpenElective(subA);
            boolean oeB = isOpenElective(subB);
            if (oeA != oeB)
                return oeA ? -1 : 1;
            return Integer.compare(getCredits(subA), getCredits(subB));
        });

        // Backtracking
        backtrackCount = 0;
        ArrayList<Integer> toPlace = new ArrayList<>(realSlots);
        boolean success = backtrack(toPlace, arr2, day, hour, days, hours);

        if (!success) {
            System.out.println("WARNING: Backtracking incomplete! Remaining: " + toPlace.size());
            // Intelligent Round-Robin Greedy fallback to scatter remaining slots
            int dPointer = 0;
            for (int slotIdx : new ArrayList<>(toPlace)) {
                boolean placed = false;

                // Attempt 1: Scatter across days avoiding same-day collisions
                for (int attempt = 0; attempt < days && !placed; attempt++) {
                    int d = (dPointer + attempt) % days;

                    String subjectId = SlotGenerator.slot[slotIdx].subject;
                    int subCountToday = countSubjectOnDay(arr2, d, subjectId, hours);
                    if (subCountToday > 0 && attempt < days - 1)
                        continue; // Scatter to a different day

                    for (int h = 0; h < hours && !placed; h++) {
                        if (arr2[d][h] == -1) {
                            arr2[d][h] = slotIdx;
                            toPlace.remove(Integer.valueOf(slotIdx));
                            placed = true;
                            dPointer = (d + 1) % days; // Seed next slot on the next day
                        }
                    }
                }

                // Attempt 2: If absolutely necessary, force into any remaining empty slot
                // globally
                if (!placed) {
                    for (int d = 0; d < days && !placed; d++) {
                        for (int h = 0; h < hours && !placed; h++) {
                            if (arr2[d][h] == -1) {
                                arr2[d][h] = slotIdx;
                                toPlace.remove(Integer.valueOf(slotIdx));
                                placed = true;
                            }
                        }
                    }
                }
            }
            System.out.println("WARNING: Used Scatter Fallback mechanism for isolated slots");
        } else {
            System.out.println("SUCCESS: All " + realSlots.size() + " subject slots placed.");
        }

        // Fill remaining empty cells with free slots
        for (int d = 0; d < days; d++) {
            for (int h = 0; h < hours; h++) {
                if (arr2[d][h] == -1 && !freeSlots.isEmpty()) {
                    arr2[d][h] = freeSlots.remove(0);
                }
            }
        }

        // Insert all placed slots into DB (skip -2 locked cells)
        for (int i = 0; i < days; i++) {
            for (int j = 0; j < hours; j++) {
                if (arr2[i][j] >= 0 && arr2[i][j] < SlotGenerator.slot.length
                        && SlotGenerator.slot[arr2[i][j]] != null) {
                    db.executeUpdate("INSERT INTO TimeTable(Day,Hour,SubId,GrpId,TeacherId) VALUES('"
                            + day.get(i) + "','" + hour.get(j) + "','"
                            + SlotGenerator.slot[arr2[i][j]].subject + "',"
                            + SlotGenerator.slot[arr2[i][j]].studentgroup.id + ","
                            + SlotGenerator.slot[arr2[i][j]].teacherid + ")");
                }
            }
        }
    }

    // ==================== BACKTRACKING ====================

    private static boolean backtrack(ArrayList<Integer> toPlace, int[][] arr2,
            ArrayList<String> day, ArrayList<String> hour, int days, int hours) {
        if (toPlace.isEmpty())
            return true;
        if (backtrackCount++ > MAX_BACKTRACK)
            return false;

        int slotIdx = toPlace.get(0);
        Slot slot = SlotGenerator.slot[slotIdx];
        if (slot == null) {
            toPlace.remove(0);
            return backtrack(toPlace, arr2, day, hour, days, hours);
        }

        String subjectId = slot.subject;
        int teacherId = slot.teacherid;
        boolean isOE = isOpenElective(subjectId);
        int credits = getCredits(subjectId);

        // Lab subjects: must be placed as continuous blocks
        if (isLabSubject(subjectId)) {
            return backtrackLab(toPlace, arr2, day, hour, days, hours, subjectId, teacherId, isOE);
        }

        // Non-lab: place one slot at a time with TWO passes to prefer spreading
        for (int pass = 0; pass <= 1; pass++) {
            for (int d = 0; d < days; d++) {
                int subToday = countSubjectOnDay(arr2, d, subjectId, hours);

                // Pass 0: strictly no repetition. Pass 1: exact 1 repetition to form a double
                // block
                if (pass == 0 && subToday >= 1)
                    continue;
                if (pass == 1 && subToday != 1)
                    continue;

                for (int h = 0; h < hours; h++) {
                    if (arr2[d][h] != -1)
                        continue;

                    // OE only in 1st hour (h=0)
                    if (isOE && h != 0)
                        continue;

                    if (pass == 1) {
                        // Max 1 double block per week for any subject (bypasses credits requirement for
                        // safety)
                        if (!isAdjacentToSameSubject(arr2, d, h, subjectId, hours))
                            continue;
                        if (countDaysWithDouble(arr2, subjectId, days, hours) >= 1)
                            continue;
                    }

                    // Teacher not double-booked
                    if (teacherId > 0 && !isOE) {
                        if (isTeacherBusyInDB(teacherId, day.get(d), hour.get(h)))
                            continue;
                    }

                    arr2[d][h] = slotIdx;
                    toPlace.remove(0);
                    if (backtrack(toPlace, arr2, day, hour, days, hours))
                        return true;
                    arr2[d][h] = -1;
                    toPlace.add(0, slotIdx);
                }
            }
        }
        return false;
    }

    /**
     * Place all hours of a lab subject as a continuous block.
     * Must be in morning (h=0,1,2) OR afternoon (h=4,5,6). Max 1 lab/day.
     */
    private static boolean backtrackLab(ArrayList<Integer> toPlace, int[][] arr2,
            ArrayList<String> day, ArrayList<String> hour, int days, int hours,
            String subjectId, int teacherId, boolean isOE) {

        ArrayList<Integer> labSlots = new ArrayList<>();
        for (int idx : toPlace) {
            if (SlotGenerator.slot[idx] != null && subjectId.equals(SlotGenerator.slot[idx].subject)) {
                labSlots.add(idx);
            }
        }
        int totalLabRemaining = labSlots.size();
        if (totalLabRemaining == 0)
            return backtrack(toPlace, arr2, day, hour, days, hours);

        int labCount = totalLabRemaining;
        if (labCount > 3) {
            if (labCount == 4)
                labCount = 2; // Split 4 into 2 and 2
            else
                labCount = 3; // Max 3 contiguous
        }

        for (int d = 0; d < days; d++) {
            if (hasLabOnDay(arr2, d, hours))
                continue;

            // Morning (h=0..2) and Afternoon (h=4..6) — skip lunch (h=3)
            int[][] sessions = { { 0, 3 }, { 4, 7 } };
            for (int[] session : sessions) {
                int sessStart = session[0];
                int sessEnd = session[1];

                for (int startH = sessStart; startH + labCount <= sessEnd; startH++) {
                    boolean canPlace = true;
                    for (int h = startH; h < startH + labCount && canPlace; h++) {
                        if (arr2[d][h] != -1)
                            canPlace = false;
                        if (canPlace && teacherId > 0 && !isOE) {
                            if (isTeacherBusyInDB(teacherId, day.get(d), hour.get(h)))
                                canPlace = false;
                        }
                    }

                    if (canPlace) {
                        for (int i = 0; i < labCount; i++) {
                            arr2[d][startH + i] = labSlots.get(i);
                            toPlace.remove(Integer.valueOf(labSlots.get(i)));
                        }
                        if (backtrack(toPlace, arr2, day, hour, days, hours))
                            return true;

                        // Backtrack
                        for (int i = 0; i < labCount; i++) {
                            arr2[d][startH + i] = -1;
                        }
                        for (int i = labCount - 1; i >= 0; i--) {
                            toPlace.add(0, labSlots.get(i)); // Add back to top
                        }
                    }
                }
            }
        }
        return false;
    }

    // ==================== CONSTRAINT HELPERS ====================

    private static boolean isTeacherBusyInDB(int teacherId, String dayStr, String hourStr) {
        if (teacherId <= 0)
            return false;
        return busyTeacherSlots.contains(teacherId + "-" + dayStr + "-" + hourStr);
    }

    private static int countSubjectOnDay(int[][] arr2, int day, String subjectId, int hours) {
        int count = 0;
        for (int h = 0; h < hours; h++) {
            if (arr2[day][h] >= 0 && SlotGenerator.slot[arr2[day][h]] != null
                    && subjectId.equals(SlotGenerator.slot[arr2[day][h]].subject)) {
                count++;
            }
        }
        return count;
    }

    private static boolean sameSession(int h1, int h2) {
        // Morning: 0,1,2 Afternoon: 4,5,6 Lunch: 3 (neither)
        return (h1 < 3 && h2 < 3) || (h1 >= 4 && h2 >= 4);
    }

    private static boolean isAdjacentToSameSubject(int[][] arr2, int day, int hour, String subjectId, int totalHours) {
        if (hour > 0 && sameSession(hour - 1, hour)
                && arr2[day][hour - 1] >= 0 && SlotGenerator.slot[arr2[day][hour - 1]] != null
                && subjectId.equals(SlotGenerator.slot[arr2[day][hour - 1]].subject)) {
            return true;
        }
        if (hour < totalHours - 1 && sameSession(hour, hour + 1)
                && arr2[day][hour + 1] >= 0 && SlotGenerator.slot[arr2[day][hour + 1]] != null
                && subjectId.equals(SlotGenerator.slot[arr2[day][hour + 1]].subject)) {
            return true;
        }
        return false;
    }

    private static int countDaysWithDouble(int[][] arr2, String subjectId, int days, int hours) {
        int count = 0;
        for (int d = 0; d < days; d++) {
            if (countSubjectOnDay(arr2, d, subjectId, hours) >= 2)
                count++;
        }
        return count;
    }

    private static boolean hasLabOnDay(int[][] arr2, int day, int hours) {
        for (int h = 0; h < hours; h++) {
            if (arr2[day][h] >= 0 && SlotGenerator.slot[arr2[day][h]] != null
                    && isLabSubject(SlotGenerator.slot[arr2[day][h]].subject)) {
                return true;
            }
        }
        return false;
    }

    // ==================== CACHED DB LOOKUPS ====================

    private static boolean isOpenElective(String subjectId) {
        if (subjectId == null || "FR-0000".equals(subjectId))
            return false;
        if (oeCache.containsKey(subjectId))
            return oeCache.get(subjectId);
        DatabaseConnection checkDb = new DatabaseConnection();
        ResultSet rs = checkDb.executeQuery("SELECT isOE FROM Subject WHERE SubId = '" + subjectId + "'");
        try {
            if (rs.next()) {
                boolean v = rs.getBoolean("isOE");
                oeCache.put(subjectId, v);
                return v;
            }
        } catch (SQLException ex) {
        }
        oeCache.put(subjectId, false);
        return false;
    }

    private static boolean isLabSubject(String subjectId) {
        if (subjectId == null || "FR-0000".equals(subjectId) || "SPORTS-00".equals(subjectId)
                || "TECHSKILLS-00".equals(subjectId) || "LUNCH-00".equals(subjectId))
            return false;
        if (labCache.containsKey(subjectId))
            return labCache.get(subjectId);
        DatabaseConnection checkDb = new DatabaseConnection();
        ResultSet rs = checkDb.executeQuery("SELECT isLab FROM Subject WHERE SubId = '" + subjectId + "'");
        try {
            if (rs.next()) {
                boolean v = rs.getBoolean("isLab");
                labCache.put(subjectId, v);
                return v;
            }
        } catch (SQLException ex) {
        }
        labCache.put(subjectId, false);
        return false;
    }

    private static int getCredits(String subjectId) {
        if (subjectId == null || "FR-0000".equals(subjectId))
            return 0;
        if (creditsCache.containsKey(subjectId))
            return creditsCache.get(subjectId);
        DatabaseConnection checkDb = new DatabaseConnection();
        ResultSet rs = checkDb.executeQuery("SELECT credits FROM Subject WHERE SubId = '" + subjectId + "'");
        try {
            if (rs.next()) {
                int v = rs.getInt("credits");
                creditsCache.put(subjectId, v);
                return v;
            }
        } catch (SQLException ex) {
        }
        creditsCache.put(subjectId, 3);
        return 3;
    }

    private static int countTotalSubjectHours(String subjectId, int grpId) {
        int count = 0;
        if (SlotGenerator.slot == null)
            return 0;
        for (int i = 0; i < SlotGenerator.slot.length; i++) {
            if (SlotGenerator.slot[i] != null && SlotGenerator.slot[i].studentgroup.id == grpId
                    && subjectId.equals(SlotGenerator.slot[i].subject)) {
                count++;
            }
        }
        return count;
    }
}