package firstphase;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin-logout")
public class AdminLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            // Clears ONLY the admin attributes; a student login in the same browser is untouched
            session.removeAttribute("adminId");
            session.removeAttribute("adminUsername");
            session.removeAttribute("adminPortalTitle");
            session.removeAttribute("adminTimeoutMinutes");
        }
        resp.sendRedirect(req.getContextPath() + "/admin-login.jsp?msg=loggedout");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }
}
