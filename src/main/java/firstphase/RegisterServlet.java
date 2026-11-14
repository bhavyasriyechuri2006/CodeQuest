package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = trim(req.getParameter("username"));
        String email = trim(req.getParameter("email")).toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirmPassword") == null ? "" : req.getParameter("confirmPassword");

        List<String> errors = new ArrayList<>();
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            errors.add("All fields are required.");
        }
        if (!username.isEmpty() && !username.matches("[A-Za-z0-9_]{3,30}")) {
            errors.add("Username must be 3-30 characters: letters, digits or underscore only.");
        }
        if (!email.isEmpty() && (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") || email.length() > 100)) {
            errors.add("Please enter a valid email address.");
        }
        if (!password.isEmpty() && password.length() < 6) {
            errors.add("Password must be at least 6 characters long.");
        }
        if (!password.isEmpty() && !confirm.isEmpty() && !password.equals(confirm)) {
            errors.add("Password and confirm password do not match.");
        }

        if (errors.isEmpty()) {
            try (Connection con = DBConnection.getConnection()) {
                // Uniqueness check
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT username, email FROM users WHERE username = ? OR email = ?")) {
                    ps.setString(1, username);
                    ps.setString(2, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (username.equalsIgnoreCase(rs.getString("username"))
                                    && !errors.contains("That username is already taken.")) {
                                errors.add("That username is already taken.");
                            }
                            if (email.equalsIgnoreCase(rs.getString("email"))
                                    && !errors.contains("That email address is already registered.")) {
                                errors.add("That email address is already registered.");
                            }
                        }
                    }
                }
                if (errors.isEmpty()) {
                    // New students start with 0 XP and Level 1
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO users (username, email, password, total_xp, player_level) VALUES (?, ?, ?, 0, 1)")) {
                        ps.setString(1, username);
                        ps.setString(2, email);
                        ps.setString(3, DBConnection.hash(password));
                        ps.executeUpdate();
                    }
                    // sendRedirect: navigation to a new request after success
                    resp.sendRedirect(req.getContextPath() + "/login.jsp?msg=registered");
                    return;
                }
            } catch (SQLIntegrityConstraintViolationException e) {
                errors.add("That username or email is already registered.");
            } catch (SQLException e) {
                errors.add("Database error: " + e.getMessage() + " (is MySQL running and the password correct in DBConnection.java?)");
            }
        }

        // RequestDispatcher.forward: show errors inline on the same page
        req.setAttribute("errors", errors);
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
