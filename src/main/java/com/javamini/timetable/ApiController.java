package com.javamini.timetable;

import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    // ==================== TEACHERS ====================

    @GetMapping("/teachers")
    public List<Map<String, Object>> getTeachers() {
        List<Map<String, Object>> list = new ArrayList<>();
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery("SELECT * FROM Teacher ORDER BY TeacherId");
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("TeacherId"));
                m.put("name", rs.getString("TeacherName"));
                m.put("assigned", rs.getInt("assign"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @PostMapping("/teachers")
    public Map<String, Object> addTeacher(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("INSERT INTO Teacher(TeacherName, assign) VALUES('" + name + "',0)");
        return Map.of("status", "ok", "message", "Teacher '" + name + "' added");
    }

    @DeleteMapping("/teachers/{id}")
    public Map<String, Object> deleteTeacher(@PathVariable int id) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("DELETE FROM TeacherSubject WHERE TeacherId = " + id);
        db.executeUpdate("DELETE FROM Teacher WHERE TeacherId = " + id);
        return Map.of("status", "ok");
    }

    @GetMapping("/teachers/{id}/subjects")
    public List<Map<String, Object>> getTeacherSubjects(@PathVariable int id) {
        List<Map<String, Object>> list = new ArrayList<>();
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery(
            "SELECT ts.SubId, s.SubName FROM TeacherSubject ts " +
            "JOIN Subject s ON ts.SubId = s.SubId WHERE ts.TeacherId = " + id);
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("subId", rs.getString("SubId"));
                m.put("subName", rs.getString("SubName"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @PostMapping("/teachers/{id}/subjects")
    public Map<String, Object> assignTeacherSubject(@PathVariable int id, @RequestBody Map<String, String> body) {
        String subId = body.get("subId");
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("INSERT INTO TeacherSubject(TeacherId, SubId) VALUES(" + id + ",'" + subId + "')");
        return Map.of("status", "ok");
    }

    @DeleteMapping("/teachers/{teacherId}/subjects/{subId}")
    public Map<String, Object> removeTeacherSubject(@PathVariable int teacherId, @PathVariable String subId) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("DELETE FROM TeacherSubject WHERE TeacherId = " + teacherId + " AND SubId = '" + subId + "'");
        return Map.of("status", "ok");
    }

    // ==================== SUBJECTS ====================

    @GetMapping("/subjects")
    public List<Map<String, Object>> getSubjects() {
        List<Map<String, Object>> list = new ArrayList<>();
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery("SELECT * FROM Subject ORDER BY SubId");
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("subId", rs.getString("SubId"));
                m.put("subName", rs.getString("SubName"));
                try { m.put("credits", rs.getInt("credits")); } catch (SQLException e) { m.put("credits", 3); }
                try { m.put("isLab", rs.getBoolean("isLab")); } catch (SQLException e) { m.put("isLab", false); }
                try { m.put("isOE", rs.getBoolean("isOE")); } catch (SQLException e) { m.put("isOE", false); }
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @PostMapping("/subjects")
    public Map<String, Object> addSubject(@RequestBody Map<String, Object> body) {
        String subName = (String) body.get("subName");
        String subId = (String) body.get("subId");
        int credits = body.get("credits") instanceof Number ? ((Number) body.get("credits")).intValue() : 3;
        boolean isLab = Boolean.TRUE.equals(body.get("isLab"));
        boolean isOE = Boolean.TRUE.equals(body.get("isOE"));
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("INSERT INTO Subject(SubName, SubId, credits, isLab, isOE) VALUES('" +
                subName + "','" + subId + "'," + credits + "," + isLab + "," + isOE + ")");
        return Map.of("status", "ok", "message", "Subject '" + subName + "' added");
    }

    @DeleteMapping("/subjects/{subId}")
    public Map<String, Object> deleteSubject(@PathVariable String subId) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("DELETE FROM TeacherSubject WHERE SubId = '" + subId + "'");
        db.executeUpdate("DELETE FROM SubHrs WHERE SubId = '" + subId + "'");
        db.executeUpdate("DELETE FROM Subject WHERE SubId = '" + subId + "'");
        return Map.of("status", "ok");
    }

    // ==================== GROUPS ====================

    @GetMapping("/groups")
    public List<Map<String, Object>> getGroups() {
        List<Map<String, Object>> list = new ArrayList<>();
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery("SELECT * FROM StudentGroup ORDER BY GrpId");
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("grpId", rs.getInt("GrpId"));
                m.put("grpName", rs.getString("GrpName"));
                m.put("noOfSub", rs.getInt("NoOfSub"));
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @PostMapping("/groups")
    public Map<String, Object> addGroup(@RequestBody Map<String, Object> body) {
        String grpName = (String) body.get("grpName");
        int noOfSub = body.get("noOfSub") instanceof Number ? ((Number) body.get("noOfSub")).intValue() : 0;
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("INSERT INTO StudentGroup(GrpName, NoOfSub) VALUES('" + grpName + "'," + noOfSub + ")");
        return Map.of("status", "ok", "message", "Class '" + grpName + "' added");
    }

    @DeleteMapping("/groups/{id}")
    public Map<String, Object> deleteGroup(@PathVariable int id) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("DELETE FROM SubHrs WHERE GrpId = " + id);
        db.executeUpdate("DELETE FROM TimeTable WHERE GrpId = " + id);
        db.executeUpdate("DELETE FROM StudentGroup WHERE GrpId = " + id);
        return Map.of("status", "ok");
    }

    @GetMapping("/groups/{id}/subjects")
    public List<Map<String, Object>> getGroupSubjects(@PathVariable int id) {
        List<Map<String, Object>> list = new ArrayList<>();
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery(
            "SELECT sh.SubId, s.SubName, sh.Hours, sh.TeacherId FROM SubHrs sh " +
            "JOIN Subject s ON sh.SubId = s.SubId WHERE sh.GrpId = " + id);
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("subId", rs.getString("SubId"));
                m.put("subName", rs.getString("SubName"));
                m.put("hours", rs.getInt("Hours"));
                int tid = rs.getInt("TeacherId");
                m.put("teacherId", tid);
                if (tid > 0) {
                    m.put("teacherName", new Teacher().getTeacherName(tid));
                } else {
                    m.put("teacherName", tid == -1 ? "N/A (OE)" : "Unassigned");
                }
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @PostMapping("/groups/{id}/subjects")
    public Map<String, Object> assignGroupSubject(@PathVariable int id, @RequestBody Map<String, Object> body) {
        String subId = (String) body.get("subId");
        int hours = body.get("hours") instanceof Number ? ((Number) body.get("hours")).intValue() : 3;
        int teacherId = body.get("teacherId") instanceof Number ? ((Number) body.get("teacherId")).intValue() : 0;
        DatabaseConnection db = new DatabaseConnection();
        // Remove existing assignment if any
        db.executeUpdate("DELETE FROM SubHrs WHERE GrpId = " + id + " AND SubId = '" + subId + "'");
        db.executeUpdate("INSERT INTO SubHrs(GrpId, SubId, Hours, TeacherId) VALUES(" + id + ",'" + subId + "'," + hours + ", " + teacherId + ")");
        // Update NoOfSub count
        ResultSet rs = db.executeQuery("SELECT COUNT(*) as cnt FROM SubHrs WHERE GrpId = " + id);
        try {
            if (rs != null && rs.next()) {
                db.executeUpdate("UPDATE StudentGroup SET NoOfSub = " + rs.getInt("cnt") + " WHERE GrpId = " + id);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Map.of("status", "ok");
    }

    @DeleteMapping("/groups/{grpId}/subjects/{subId}")
    public Map<String, Object> removeGroupSubject(@PathVariable int grpId, @PathVariable String subId) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("DELETE FROM SubHrs WHERE GrpId = " + grpId + " AND SubId = '" + subId + "'");
        // Update NoOfSub count
        ResultSet rs = db.executeQuery("SELECT COUNT(*) as cnt FROM SubHrs WHERE GrpId = " + grpId);
        try {
            if (rs != null && rs.next()) {
                db.executeUpdate("UPDATE StudentGroup SET NoOfSub = " + rs.getInt("cnt") + " WHERE GrpId = " + grpId);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Map.of("status", "ok");
    }

    // ==================== TIMETABLE ====================

    @PostMapping("/timetable/generate/{grpId}")
    public Map<String, Object> generateTimetable(@PathVariable int grpId) {
        try {
            TimetableService service = new TimetableService();
            String result = service.generate(grpId);
            return Map.of("status", "ok", "message", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @GetMapping("/timetable/{grpId}")
    public Map<String, Object> getTimetable(@PathVariable int grpId) {
        Map<String, Object> result = new HashMap<>();
        DatabaseConnection db = new DatabaseConnection();

        // Get group name
        ResultSet grpRs = db.executeQuery("SELECT GrpName FROM StudentGroup WHERE GrpId = " + grpId);
        try {
            if (grpRs != null && grpRs.next()) result.put("grpName", grpRs.getString("GrpName"));
        } catch (SQLException e) { e.printStackTrace(); }

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] hours = {"9:40-10:40", "10:40-11:40", "11:40-12:40", "12:40-1:20", "1:20-2:20", "2:20-3:20", "3:20-4:20"};
        result.put("days", days);
        result.put("hours", hours);

        // Build grid from DB
        List<List<Map<String, String>>> grid = new ArrayList<>();
        for (String day : days) {
            List<Map<String, String>> daySlots = new ArrayList<>();
            DatabaseConnection db2 = new DatabaseConnection();
            ResultSet rs = db2.executeQuery(
                "SELECT t.Hour, t.SubId, t.TeacherId, s.SubName FROM TimeTable t " +
                "JOIN Subject s ON t.SubId = s.SubId " +
                "WHERE t.GrpId = " + grpId + " AND t.Day = '" + day + "' ORDER BY t.Hour");
            // Build a map of hour -> data
            Map<String, Map<String, String>> hourMap = new LinkedHashMap<>();
            try {
                while (rs != null && rs.next()) {
                    Map<String, String> slot = new HashMap<>();
                    slot.put("subject", rs.getString("SubName"));
                    slot.put("subId", rs.getString("SubId"));
                    int tid = rs.getInt("TeacherId");
                    if (tid > 0) {
                        slot.put("teacher", new Teacher().getTeacherName(tid));
                    } else {
                        slot.put("teacher", "");
                    }
                    // Determine type
                    String subId = rs.getString("SubId");
                    if ("LUNCH-00".equals(subId)) slot.put("type", "lunch");
                    else if ("SPORTS-00".equals(subId)) slot.put("type", "sports");
                    else if ("TECHSKILLS-00".equals(subId)) slot.put("type", "techskills");
                    else if ("FR-0000".equals(subId)) slot.put("type", "free");
                    else slot.put("type", "normal");
                    hourMap.put(rs.getString("Hour"), slot);
                }
            } catch (SQLException e) { e.printStackTrace(); }

            // Fill in order
            for (String hour : hours) {
                Map<String, String> slot = hourMap.get(hour);
                if (slot == null) {
                    slot = Map.of("subject", "", "teacher", "", "subId", "", "type", "empty");
                }
                daySlots.add(slot);
            }
            grid.add(daySlots);
        }
        result.put("grid", grid);
        return result;
    }

    @GetMapping("/timetable/teacher/{teacherId}")
    public Map<String, Object> getTeacherTimetable(@PathVariable int teacherId) {
        Map<String, Object> result = new HashMap<>();
        String teacherName = new Teacher().getTeacherName(teacherId);
        result.put("teacherName", teacherName);

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] hours = {"9:40-10:40", "10:40-11:40", "11:40-12:40", "12:40-1:20", "1:20-2:20", "2:20-3:20", "3:20-4:20"};
        result.put("days", days);
        result.put("hours", hours);

        List<List<Map<String, String>>> grid = new ArrayList<>();
        for (String day : days) {
            List<Map<String, String>> daySlots = new ArrayList<>();
            DatabaseConnection db2 = new DatabaseConnection();
            ResultSet rs = db2.executeQuery(
                "SELECT t.Hour, t.SubId, g.GrpName, s.SubName " +
                "FROM TimeTable t " +
                "JOIN Subject s ON t.SubId = s.SubId " +
                "JOIN StudentGroup g ON t.GrpId = g.GrpId " +
                "WHERE t.TeacherId = " + teacherId + " AND t.Day = '" + day + "' ORDER BY t.Hour");
            
            Map<String, Map<String, String>> hourMap = new LinkedHashMap<>();
            try {
                while (rs != null && rs.next()) {
                    Map<String, String> slot = new HashMap<>();
                    slot.put("subject", rs.getString("SubName"));
                    slot.put("groupName", rs.getString("GrpName"));
                    slot.put("type", "normal");
                    hourMap.put(rs.getString("Hour"), slot);
                }
            } catch (SQLException e) { e.printStackTrace(); }

            for (String hour : hours) {
                Map<String, String> slot = hourMap.get(hour);
                if (slot == null) {
                    slot = Map.of("subject", "-", "groupName", "", "type", "free");
                }
                daySlots.add(slot);
            }
            grid.add(daySlots);
        }
        result.put("grid", grid);
        return result;
    }

    // ==================== AUTHENTICATION ====================

    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String role = (String) body.get("role");
        Object rawGrpId = body.get("grpId");
        Object rawTeacherId = body.get("teacherId");
        
        String grpIdVal = (rawGrpId != null && !rawGrpId.toString().trim().isEmpty()) ? rawGrpId.toString() : "NULL";
        String teacherIdVal = (rawTeacherId != null && !rawTeacherId.toString().trim().isEmpty()) ? rawTeacherId.toString() : "NULL";
        boolean isApproved = "ADMIN".equals(role);
        
        DatabaseConnection db = new DatabaseConnection();
        try {
            db.executeUpdate("INSERT INTO Users(Username, Password, Role, Approved, GrpId, TeacherId) VALUES('" +
                    username + "','" + password + "','" + role + "'," + isApproved + "," + grpIdVal + "," + teacherIdVal + ")");
                    
            if (isApproved) {
                return Map.of("status", "ok", "message", "Admin account created successfully. You may login.");
            } else {
                return Map.of("status", "ok", "message", "Registration requested. Awaiting Admin approval.");
            }
        } catch (Exception e) {
            return Map.of("status", "error", "message", "Username may already exist.");
        }
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery("SELECT * FROM Users WHERE Username = '" + username + "' AND Password = '" + password + "'");
        try {
            if (rs != null && rs.next()) {
                boolean approved = rs.getBoolean("Approved");
                if (!approved) {
                    return Map.of("status", "error", "message", "Account pending Admin approval.");
                }
                Map<String, Object> user = new HashMap<>();
                user.put("id", rs.getInt("Id"));
                user.put("username", rs.getString("Username"));
                user.put("role", rs.getString("Role"));
                user.put("grpId", rs.getInt("GrpId"));
                int tid = 0;
                try { tid = rs.getInt("TeacherId"); } catch (SQLException e) {}
                user.put("teacherId", tid);
                return Map.of("status", "ok", "user", user);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return Map.of("status", "error", "message", "Invalid credentials.");
    }

    @GetMapping("/auth/pending")
    public List<Map<String, Object>> getPendingUsers() {
        List<Map<String, Object>> list = new ArrayList<>();
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery("SELECT u.*, g.GrpName FROM Users u LEFT JOIN StudentGroup g ON u.GrpId = g.GrpId WHERE u.Approved = false");
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("Id"));
                m.put("username", rs.getString("Username"));
                m.put("role", rs.getString("Role"));
                m.put("grpName", rs.getString("GrpName") != null ? rs.getString("GrpName") : "N/A");
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @PostMapping("/auth/approve/{userId}")
    public Map<String, Object> approveUser(@PathVariable int userId) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("UPDATE Users SET Approved = true WHERE Id = " + userId);
        return Map.of("status", "ok");
    }

    @DeleteMapping("/auth/reject/{userId}")
    public Map<String, Object> rejectUser(@PathVariable int userId) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("DELETE FROM Users WHERE Id = " + userId);
        return Map.of("status", "ok");
    }

    // ==================== NOTIFICATIONS ====================

    @GetMapping("/notifications/{grpId}")
    public List<Map<String, Object>> getNotifications(@PathVariable int grpId) {
        List<Map<String, Object>> list = new ArrayList<>();
        DatabaseConnection db = new DatabaseConnection();
        ResultSet rs = db.executeQuery("SELECT * FROM Notifications WHERE GrpId = " + grpId + " ORDER BY DatePosted DESC");
        try {
            while (rs != null && rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", rs.getInt("Id"));
                m.put("message", rs.getString("Message"));
                m.put("author", rs.getString("Author"));
                m.put("date", rs.getTimestamp("DatePosted").toString());
                list.add(m);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @PostMapping("/notifications")
    public Map<String, Object> createNotification(@RequestBody Map<String, Object> body) {
        int grpId = body.get("grpId") instanceof Number ? ((Number) body.get("grpId")).intValue() : 0;
        String message = (String) body.get("message");
        String author = (String) body.get("author");
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("INSERT INTO Notifications(GrpId, Message, Author) VALUES(" + grpId + ", '" + message.replace("'", "''") + "', '" + author + "')");
        return Map.of("status", "ok");
    }

    @PutMapping("/notifications/{id}")
    public Map<String, Object> editNotification(@PathVariable int id, @RequestBody Map<String, String> body) {
        String message = body.get("message");
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("UPDATE Notifications SET Message = '" + message.replace("'", "''") + "' WHERE Id = " + id);
        return Map.of("status", "ok");
    }

    @DeleteMapping("/notifications/{id}")
    public Map<String, Object> deleteNotification(@PathVariable int id) {
        DatabaseConnection db = new DatabaseConnection();
        db.executeUpdate("DELETE FROM Notifications WHERE Id = " + id);
        return Map.of("status", "ok");
    }
}
