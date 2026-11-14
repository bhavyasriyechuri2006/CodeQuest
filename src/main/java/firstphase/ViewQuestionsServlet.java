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

@WebServlet("/view-questions")
public class ViewQuestionsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/admin-login.jsp?msg=required");
            return;
        }

        List<Mission> questions = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, title, category, difficulty, xp_reward FROM missions ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Mission m = new Mission();
                m.setId(rs.getInt("id"));
                m.setTitle(rs.getString("title"));
                m.setCategory(rs.getString("category"));
                m.setDifficulty(rs.getString("difficulty"));
                m.setXpReward(rs.getInt("xp_reward"));
                questions.add(m);
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.setAttribute("questions", questions);
        // RequestDispatcher: forward the data to the JSP
        req.getRequestDispatcher("/view-questions.jsp").forward(req, resp);
    }
}
