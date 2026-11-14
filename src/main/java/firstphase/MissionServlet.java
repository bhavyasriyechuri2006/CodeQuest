package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/missions")
public class MissionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=required");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");

        String category = whitelist(req.getParameter("category"), "Java", "SQL", "DSA");
        String difficulty = whitelist(req.getParameter("difficulty"), "Easy", "Medium", "Hard");

        // NOTE: correct_answer is intentionally NOT selected, so it can never reach the student's page.
        StringBuilder sql = new StringBuilder(
                "SELECT m.id, m.title, m.description, m.category, m.difficulty, m.option_a, m.option_b, "
                        + "m.option_c, m.option_d, m.xp_reward, s.id AS sub_id, s.answer AS user_answer, "
                        + "s.is_correct AS user_correct "
                        + "FROM missions m LEFT JOIN mission_submissions s "
                        + "ON s.mission_id = m.id AND s.user_id = ? WHERE 1 = 1");
        if (!category.isEmpty()) {
            sql.append(" AND m.category = ?");
        }
        if (!difficulty.isEmpty()) {
            sql.append(" AND m.difficulty = ?");
        }
        sql.append(" ORDER BY FIELD(m.category, 'Java', 'SQL', 'DSA'), "
                + "FIELD(m.difficulty, 'Easy', 'Medium', 'Hard'), m.id");

        List<Mission> missions = new ArrayList<>();
        int attemptedCount = 0;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, userId);
            if (!category.isEmpty()) {
                ps.setString(idx++, category);
            }
            if (!difficulty.isEmpty()) {
                ps.setString(idx++, difficulty);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Mission m = new Mission();
                    m.setId(rs.getInt("id"));
                    m.setTitle(rs.getString("title"));
                    m.setDescription(rs.getString("description"));
                    m.setCategory(rs.getString("category"));
                    m.setDifficulty(rs.getString("difficulty"));
                    m.setOptionA(rs.getString("option_a"));
                    m.setOptionB(rs.getString("option_b"));
                    m.setOptionC(rs.getString("option_c"));
                    m.setOptionD(rs.getString("option_d"));
                    m.setXpReward(rs.getInt("xp_reward"));
                    if (rs.getObject("sub_id") != null) {
                        m.setAttempted(true);
                        m.setWasCorrect(rs.getBoolean("user_correct"));
                        m.setUserAnswer(rs.getString("user_answer"));
                        attemptedCount++;
                    }
                    missions.add(m);
                }
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }

        req.setAttribute("missions", missions);
        req.setAttribute("selectedCategory", category);
        req.setAttribute("selectedDifficulty", difficulty);
        req.setAttribute("attemptedCount", attemptedCount);
        req.getRequestDispatcher("/missions.jsp").forward(req, resp);
    }

    private static String whitelist(String value, String... allowed) {
        if (value != null) {
            for (String a : allowed) {
                if (a.equals(value)) {
                    return a;
                }
            }
        }
        return "";
    }
}
