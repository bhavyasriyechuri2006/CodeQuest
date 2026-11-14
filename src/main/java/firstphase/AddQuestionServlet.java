package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/add-question")
public class AddQuestionServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/admin-login.jsp?msg=required");
            return;
        }
        req.getRequestDispatcher("/add-question.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendRedirect(req.getContextPath() + "/admin-login.jsp?msg=required");
            return;
        }
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        int adminId = (Integer) session.getAttribute("adminId");

        String title = p(req, "title");
        String description = p(req, "description");
        String category = p(req, "category");
        String difficulty = p(req, "difficulty");
        String a = p(req, "optionA");
        String b = p(req, "optionB");
        String c = p(req, "optionC");
        String d = p(req, "optionD");
        String correctOption = p(req, "correctOption");
        String xpText = p(req, "xpReward");

        List<String> errors = new ArrayList<>();
        if (title.isEmpty() || description.isEmpty() || a.isEmpty() || b.isEmpty() || c.isEmpty() || d.isEmpty()) {
            errors.add("Title, description and all four answer choices are required.");
        }
        if (title.length() > 100) {
            errors.add("Title must be at most 100 characters.");
        }
        if (a.length() > 255 || b.length() > 255 || c.length() > 255 || d.length() > 255) {
            errors.add("Each answer choice must be at most 255 characters.");
        }
        if (!(category.equals("Java") || category.equals("SQL") || category.equals("DSA"))) {
            errors.add("Please choose a category (Java, SQL or DSA).");
        }
        if (!(difficulty.equals("Easy") || difficulty.equals("Medium") || difficulty.equals("Hard"))) {
            errors.add("Please choose a difficulty (Easy, Medium or Hard).");
        }
        Set<String> distinct = new HashSet<>();
        distinct.add(a.toLowerCase());
        distinct.add(b.toLowerCase());
        distinct.add(c.toLowerCase());
        distinct.add(d.toLowerCase());
        if (!a.isEmpty() && !b.isEmpty() && !c.isEmpty() && !d.isEmpty() && distinct.size() < 4) {
            errors.add("The four answer choices must all be different.");
        }
        String correctText = null;
        switch (correctOption) {
            case "A": correctText = a; break;
            case "B": correctText = b; break;
            case "C": correctText = c; break;
            case "D": correctText = d; break;
            default: errors.add("Please select which option is the correct answer.");
        }
        int xp = 0;
        try {
            xp = Integer.parseInt(xpText);
            if (xp < 10 || xp > 500) {
                errors.add("XP reward must be between 10 and 500.");
            }
        } catch (NumberFormatException e) {
            errors.add("XP reward must be a whole number.");
        }

        if (errors.isEmpty()) {
            String sql = "INSERT INTO missions (title, description, category, difficulty, option_a, option_b, "
                    + "option_c, option_d, correct_answer, xp_reward, created_by) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, title);
                ps.setString(2, description);
                ps.setString(3, category);
                ps.setString(4, difficulty);
                ps.setString(5, a);
                ps.setString(6, b);
                ps.setString(7, c);
                ps.setString(8, d);
                ps.setString(9, correctText);
                ps.setInt(10, xp);
                ps.setInt(11, adminId);
                ps.executeUpdate();
                resp.sendRedirect(req.getContextPath() + "/view-questions?msg=added");
                return;
            } catch (SQLException e) {
                errors.add("Database error: " + e.getMessage());
            }
        }
        req.setAttribute("errors", errors);
        req.getRequestDispatcher("/add-question.jsp").forward(req, resp); // params stay available for refill
    }

    private static boolean isAdmin(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null && s.getAttribute("adminId") != null;
    }

    private static String p(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }
}
