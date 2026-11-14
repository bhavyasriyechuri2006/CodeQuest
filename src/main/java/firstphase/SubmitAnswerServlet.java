package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/submit-answer")
public class SubmitAnswerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/missions");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=required");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");
        String base = req.getContextPath() + "/missions?msg=";

        int missionId;
        try {
            missionId = Integer.parseInt(req.getParameter("missionId"));
        } catch (NumberFormatException e) {
            resp.sendRedirect(base + "invalid");
            return;
        }
        String answer = req.getParameter("answer");
        if (answer == null || answer.isEmpty()) {
            resp.sendRedirect(base + "noanswer");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false); // ---- JDBC TRANSACTION starts ----
            try {
                // 1) Read the mission and its correct answer from the database
                String title, correct, a, b, c, d;
                int missionXp;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT title, correct_answer, option_a, option_b, option_c, option_d, xp_reward "
                                + "FROM missions WHERE id = ?")) {
                    ps.setInt(1, missionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            resp.sendRedirect(base + "notfound");
                            return;
                        }
                        title = rs.getString("title");
                        correct = rs.getString("correct_answer");
                        a = rs.getString("option_a");
                        b = rs.getString("option_b");
                        c = rs.getString("option_c");
                        d = rs.getString("option_d");
                        missionXp = rs.getInt("xp_reward");
                    }
                }
                if (!(answer.equals(a) || answer.equals(b) || answer.equals(c) || answer.equals(d))) {
                    con.rollback();
                    resp.sendRedirect(base + "invalid");
                    return;
                }

                // 2) Lock the student's row (serialises double-clicks) and read current XP / level
                int oldXp;
                int oldLevel;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT total_xp, player_level FROM users WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            session.invalidate();
                            resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=required");
                            return;
                        }
                        oldXp = rs.getInt("total_xp");
                        oldLevel = rs.getInt("player_level");
                    }
                }

                // 3) One-attempt-per-mission rule
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id FROM mission_submissions WHERE user_id = ? AND mission_id = ?")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, missionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            con.rollback();
                            resp.sendRedirect(base + "already");
                            return;
                        }
                    }
                }

                // 4) Record the submission (wrong answers are recorded too: that is the single attempt)
                boolean isCorrect = answer.equals(correct);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO mission_submissions (user_id, mission_id, answer, is_correct) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, missionId);
                    ps.setString(3, answer);
                    ps.setBoolean(4, isCorrect);
                    ps.executeUpdate();
                }

                // 5) Award XP and recalculate the level only when correct
                int newXp = oldXp;
                int newLevel = oldLevel;
                int earned = 0;
                if (isCorrect) {
                    earned = missionXp;
                    newXp = oldXp + missionXp;
                    newLevel = DBConnection.levelForXp(newXp);
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE users SET total_xp = ?, player_level = ? WHERE id = ?")) {
                        ps.setInt(1, newXp);
                        ps.setInt(2, newLevel);
                        ps.setInt(3, userId);
                        ps.executeUpdate();
                    }
                }

                con.commit(); // ---- both the submission AND the XP update are saved together ----

                req.setAttribute("resMissionTitle", title);
                req.setAttribute("resCorrect", isCorrect);
                req.setAttribute("resXpEarned", earned);
                req.setAttribute("resMissionXp", missionXp);
                req.setAttribute("resLeveledUp", newLevel > oldLevel);
                req.setAttribute("resNewLevel", newLevel);
                req.setAttribute("resNewTitle", DBConnection.titleForLevel(newLevel));
                req.setAttribute("resTotalXp", newXp);
                req.setAttribute("resYourAnswer", answer);
                req.setAttribute("resCorrectAnswer", correct);
                req.getRequestDispatcher("/mission-result.jsp").forward(req, resp);
            } catch (SQLIntegrityConstraintViolationException dup) {
                con.rollback(); // unique_user_mission fired: already attempted
                resp.sendRedirect(base + "already");
            } catch (SQLException e) {
                con.rollback(); // any failure: undo everything
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            resp.sendRedirect(base + "dberror");
        }
    }
}
