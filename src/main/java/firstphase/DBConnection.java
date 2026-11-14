package firstphase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Reusable, singleton-style database helper used by ALL servlets.
 * On the first call to getConnection() it automatically:
 *   1. creates the database  codequest_db  (if missing)
 *   2. creates the 4 tables  (if missing)
 *   3. inserts the default admin (admin / admin123) and 6 starter questions
 * so no manual SQL is required.
 */
public class DBConnection {

    // ================== CONFIGURATION ==================
    // >>> CHANGE DB_PASSWORD if your MySQL root password is not "root" <<<
    private static final String DB_HOST = "localhost";
    private static final int    DB_PORT = 3306;
    private static final String DB_NAME = "codequest_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    // ===================================================

    private static final String PARAMS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String SERVER_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + PARAMS;
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + PARAMS;

    /** Game rules shared by several servlets. */
    public static final int XP_PER_LEVEL = 200;
    public static final int MAX_LEVEL = 10;

    private static final Object LOCK = new Object();
    private static boolean initialized = false;

    private DBConnection() { }

    /** Returns a new connection to codequest_db (initialising the database on first use). */
    public static Connection getConnection() throws SQLException {
        ensureInitialized();
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void ensureInitialized() throws SQLException {
        synchronized (LOCK) {
            if (initialized) {
                return;
            }
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL driver not found. Copy mysql-connector-j JAR into WEB-INF/lib.", e);
            }

            // Step 1: create the database
            try (Connection con = DriverManager.getConnection(SERVER_URL, DB_USER, DB_PASSWORD);
                 Statement st = con.createStatement()) {
                st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME
                        + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            }

            // Step 2: create tables and seed data
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 Statement st = con.createStatement()) {

                st.executeUpdate("CREATE TABLE IF NOT EXISTS admins ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "username VARCHAR(50) UNIQUE, "
                        + "password VARCHAR(255)) ENGINE=InnoDB");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "username VARCHAR(50) UNIQUE, "
                        + "email VARCHAR(100) UNIQUE, "
                        + "password VARCHAR(255), "
                        + "total_xp INT DEFAULT 0, "
                        + "player_level INT DEFAULT 1, "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS missions ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "title VARCHAR(100), "
                        + "description TEXT, "
                        + "category VARCHAR(30), "
                        + "difficulty VARCHAR(20), "
                        + "option_a VARCHAR(255), "
                        + "option_b VARCHAR(255), "
                        + "option_c VARCHAR(255), "
                        + "option_d VARCHAR(255), "
                        + "correct_answer VARCHAR(255), "
                        + "xp_reward INT DEFAULT 50, "
                        + "created_by INT, "
                        + "FOREIGN KEY (created_by) REFERENCES admins(id) ON DELETE SET NULL) ENGINE=InnoDB");

                st.executeUpdate("CREATE TABLE IF NOT EXISTS mission_submissions ("
                        + "id INT PRIMARY KEY AUTO_INCREMENT, "
                        + "user_id INT, "
                        + "mission_id INT, "
                        + "answer VARCHAR(255), "
                        + "is_correct BOOLEAN DEFAULT FALSE, "
                        + "completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                        + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                        + "FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE, "
                        + "UNIQUE KEY unique_user_mission (user_id, mission_id)) ENGINE=InnoDB");

                // Default admin (password stored as SHA-256 hash)
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO admins (username, password) VALUES (?, ?)")) {
                    ps.setString(1, "admin");
                    ps.setString(2, hash("admin123"));
                    ps.executeUpdate();
                }

                int adminId = 0;
                try (PreparedStatement ps = con.prepareStatement("SELECT id FROM admins WHERE username = ?")) {
                    ps.setString(1, "admin");
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            adminId = rs.getInt(1);
                        }
                    }
                }

                // Starter questions, only if the missions table is empty
                int count = 0;
                try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM missions")) {
                    if (rs.next()) {
                        count = rs.getInt(1);
                    }
                }
                if (count == 0) {
                    seedQuestions(con, adminId);
                }
            }
            initialized = true;
        }
    }

    private static void seedQuestions(Connection con, int adminId) throws SQLException {
        String sql = "INSERT INTO missions (title, description, category, difficulty, option_a, option_b, "
                + "option_c, option_d, correct_answer, xp_reward, created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            addSeed(ps, adminId, "Java Entry Point",
                    "Which method is the entry point of a standalone Java application?",
                    "Java", "Easy",
                    "public static void main(String[] args)", "public void run()",
                    "static void start()", "void main()",
                    "public static void main(String[] args)", 50);
            addSeed(ps, adminId, "String Immutability",
                    "What is printed?\n\nString s = \"Hi\";\ns.concat(\" there\");\nSystem.out.println(s);",
                    "Java", "Medium",
                    "Hi there", "Hi", "there", "Compilation error",
                    "Hi", 100);
            addSeed(ps, adminId, "Filtering Rows",
                    "Which SQL clause is used to filter rows BEFORE they are grouped?",
                    "SQL", "Easy",
                    "WHERE", "HAVING", "ORDER BY", "LIMIT",
                    "WHERE", 50);
            addSeed(ps, adminId, "Know Your Joins",
                    "Which JOIN returns ALL rows from the left table, even when there is no match in the right table?",
                    "SQL", "Medium",
                    "INNER JOIN", "LEFT JOIN", "CROSS JOIN", "NATURAL JOIN",
                    "LEFT JOIN", 100);
            addSeed(ps, adminId, "Stack Principle",
                    "Which principle does a stack data structure follow?",
                    "DSA", "Easy",
                    "FIFO (First In First Out)", "LIFO (Last In First Out)",
                    "Random access", "Priority based",
                    "LIFO (Last In First Out)", 50);
            addSeed(ps, adminId, "Binary Search Complexity",
                    "What is the worst-case time complexity of binary search on a sorted array of n elements?",
                    "DSA", "Hard",
                    "O(n)", "O(n log n)", "O(log n)", "O(1)",
                    "O(log n)", 150);
        }
    }

    private static void addSeed(PreparedStatement ps, int adminId, String title, String desc, String cat,
                                String diff, String a, String b, String c, String d, String correct, int xp)
            throws SQLException {
        ps.setString(1, title);
        ps.setString(2, desc);
        ps.setString(3, cat);
        ps.setString(4, diff);
        ps.setString(5, a);
        ps.setString(6, b);
        ps.setString(7, c);
        ps.setString(8, d);
        ps.setString(9, correct);
        ps.setInt(10, xp);
        if (adminId > 0) {
            ps.setInt(11, adminId);
        } else {
            ps.setNull(11, java.sql.Types.INTEGER);
        }
        ps.executeUpdate();
    }

    // ---------------------------------------------------------------- helpers

    /** SHA-256 hex hash used for storing passwords. */
    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** HTML-escapes text for safe output from JSP scriptlets (we do not use JSTL/EL). Null becomes "". */
    public static String esc(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }

    /** Level formula: level = (total_xp / 200) + 1, capped at 10. */
    public static int levelForXp(int xp) {
        if (xp < 0) {
            xp = 0;
        }
        return Math.min(MAX_LEVEL, xp / XP_PER_LEVEL + 1);
    }

    public static String titleForLevel(int level) {
        switch (level) {
            case 1: return "Beginner Coder";
            case 2: return "Script Kiddie";
            case 3: return "Junior Developer";
            case 4: return "Code Warrior";
            case 5: return "Debug Master";
            case 6:
            case 7: return "Algorithm Ninja";
            default: return "Code Legend";
        }
    }
}
