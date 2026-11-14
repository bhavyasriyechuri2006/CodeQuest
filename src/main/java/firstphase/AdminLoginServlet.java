package firstphase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin login. Mapped in web.xml (/admin-login) so that it can receive ServletConfig init-params.
 * The admin identity lives in its OWN session attributes (adminId / adminUsername),
 * completely separate from the student attributes (userId / username).
 */
public class AdminLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private int timeoutMinutes = 30;

    @Override
    public void init() throws ServletException {
        // ServletConfig: servlet-specific init-param from web.xml
        String value = getServletConfig().getInitParameter("adminSessionTimeoutMinutes");
        try {
            timeoutMinutes = Integer.parseInt(value.trim());
        } catch (Exception e) {
            timeoutMinutes = 30;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/admin-login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username") == null ? "" : req.getParameter("username").trim();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");

        if (username.isEmpty() || password.isEmpty()) {
            fail(req, resp, "Please enter the admin username and password.");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT id, username FROM admins WHERE username = ? AND password = ?")) {
            ps.setString(1, username);
            ps.setString(2, DBConnection.hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (req.getSession(false) != null) {
                        req.changeSessionId();
                    }
                    HttpSession session = req.getSession(true);
                    session.setAttribute("adminId", rs.getInt("id"));
                    session.setAttribute("adminUsername", rs.getString("username"));
                    session.setMaxInactiveInterval(timeoutMinutes * 60);

                    // ServletContext: application-wide value (appName) and a shared counter
                    ServletContext context = getServletContext();
                    String appName = context.getInitParameter("appName");
                    synchronized (context) {
                        Integer logins = (Integer) context.getAttribute("adminLoginCount");
                        context.setAttribute("adminLoginCount", logins == null ? 1 : logins + 1);
                    }
                    session.setAttribute("adminPortalTitle",
                            getServletConfig().getInitParameter("portalTitle") + " - " + appName);
                    session.setAttribute("adminTimeoutMinutes", timeoutMinutes);

                    resp.sendRedirect(req.getContextPath() + "/admin-dashboard.jsp");
                    return;
                }
            }
            fail(req, resp, "Invalid admin credentials.");
        } catch (SQLException e) {
            fail(req, resp, "Database error: " + e.getMessage() + " (is MySQL running?)");
        }
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String message)
            throws ServletException, IOException {
        req.setAttribute("error", message);
        req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
    }
}
