package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    public static final String REMEMBER_COOKIE = "rememberedUser";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String identifier = req.getParameter("identifier") == null ? "" : req.getParameter("identifier").trim();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        boolean remember = "on".equals(req.getParameter("remember"));

        if (identifier.isEmpty() || password.isEmpty()) {
            fail(req, resp, "Please enter your username/email and password.");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, username FROM users WHERE (username = ? OR email = ?) AND password = ?")) {
            ps.setString(1, identifier);
            ps.setString(2, identifier.toLowerCase());
            ps.setString(3, DBConnection.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    String username = rs.getString("username");

                    // HttpSession: protects against session fixation, then stores ID + username
                    if (req.getSession(false) != null) {
                        req.changeSessionId();
                    }
                    HttpSession session = req.getSession(true);
                    session.setAttribute("userId", userId);
                    session.setAttribute("username", username);
                    session.setMaxInactiveInterval(30 * 60);

                    // Cookie: "remembered user" pre-fills the login form next time
                    String path = req.getContextPath().isEmpty() ? "/" : req.getContextPath();
                    Cookie cookie = new Cookie(REMEMBER_COOKIE, username);
                    cookie.setPath(path);
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(remember ? 30 * 24 * 60 * 60 : 0); // 0 deletes the cookie
                    resp.addCookie(cookie);

                    // sendRedirect: go to the dashboard servlet as a new request
                    resp.sendRedirect(req.getContextPath() + "/dashboard");
                    return;
                }
            }
            fail(req, resp, "Invalid username/email or password.");
        } catch (SQLException e) {
            fail(req, resp, "Database error: " + e.getMessage() + " (is MySQL running?)");
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        // RequestDispatcher.forward: inline error on login.jsp
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }
}
