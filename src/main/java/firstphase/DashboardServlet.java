package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=required");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");

        try (Connection con = DBConnection.getConnection()) {
            int totalXp;
            int level;
            String username;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT username, total_xp, player_level FROM users WHERE id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        session.invalidate(); // account no longer exists
                        resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=required");
                        return;
                    }
                    username = rs.getString("username");
                    totalXp = rs.getInt("total_xp");
                    level = rs.getInt("player_level");
                }
            }

            int completed = 0;
            int attempted = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT COUNT(*) AS attempted, COALESCE(SUM(CASE WHEN is_correct THEN 1 ELSE 0 END), 0) AS correct "
                            + "FROM mission_submissions WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        attempted = rs.getInt("attempted");
                        completed = rs.getInt("correct");
                    }
                }
            }

            int totalMissions = 0;
            try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM missions");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalMissions = rs.getInt(1);
                }
            }

            boolean maxLevel = level >= DBConnection.MAX_LEVEL;
            int xpIntoLevel = maxLevel ? DBConnection.XP_PER_LEVEL
                    : totalXp - (level - 1) * DBConnection.XP_PER_LEVEL;
            if (xpIntoLevel < 0) {
                xpIntoLevel = 0;
            }
            int progress = maxLevel ? 100 : Math.min(100, xpIntoLevel * 100 / DBConnection.XP_PER_LEVEL);

            req.setAttribute("dashUsername", username);
            req.setAttribute("dashTotalXp", totalXp);
            req.setAttribute("dashLevel", level);
            req.setAttribute("dashLevelTitle", DBConnection.titleForLevel(level));
            req.setAttribute("dashNextTitle", maxLevel ? "" : DBConnection.titleForLevel(level + 1));
            req.setAttribute("dashCompleted", completed);
            req.setAttribute("dashAttempted", attempted);
            req.setAttribute("dashTotalMissions", totalMissions);
            req.setAttribute("dashXpIntoLevel", xpIntoLevel);
            req.setAttribute("dashXpPerLevel", DBConnection.XP_PER_LEVEL);
            req.setAttribute("dashProgress", progress);
            req.setAttribute("dashMaxLevel", maxLevel);
        } catch (SQLException e) {
            req.setAttribute("dashError", "Database error: " + e.getMessage());
            req.setAttribute("dashUsername", String.valueOf(session.getAttribute("username")));
            req.setAttribute("dashTotalXp", 0);
            req.setAttribute("dashLevel", 1);
            req.setAttribute("dashLevelTitle", DBConnection.titleForLevel(1));
            req.setAttribute("dashNextTitle", DBConnection.titleForLevel(2));
            req.setAttribute("dashCompleted", 0);
            req.setAttribute("dashAttempted", 0);
            req.setAttribute("dashTotalMissions", 0);
            req.setAttribute("dashXpIntoLevel", 0);
            req.setAttribute("dashXpPerLevel", DBConnection.XP_PER_LEVEL);
            req.setAttribute("dashProgress", 0);
            req.setAttribute("dashMaxLevel", false);
        }
        req.getRequestDispatcher("/dashboard.jsp").forward(req, resp);
    }
}
