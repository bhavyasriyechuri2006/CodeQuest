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

@WebServlet("/leaderboard")
public class LeaderboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sql = "SELECT u.id, u.username, u.player_level, u.total_xp, "
                + "(SELECT COUNT(*) FROM mission_submissions s WHERE s.user_id = u.id AND s.is_correct = TRUE) AS completed "
                + "FROM users u ORDER BY u.total_xp DESC, completed DESC, u.username ASC LIMIT 100";

        List<LeaderboardUser> players = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int position = 0;
            int rank = 0;
            int previousXp = -1;
            while (rs.next()) {
                position++;
                int xp = rs.getInt("total_xp");
                if (xp != previousXp) {
                    rank = position; // equal XP shares the same rank
                    previousXp = xp;
                }
                LeaderboardUser u = new LeaderboardUser();
                u.setRank(rank);
                u.setUserId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setLevel(rs.getInt("player_level"));
                u.setCompleted(rs.getInt("completed"));
                u.setTotalXp(xp);
                players.add(u);
            }
        } catch (SQLException e) {
            req.setAttribute("error", "Database error: " + e.getMessage());
        }
        req.setAttribute("players", players);
        req.getRequestDispatcher("/leaderboard.jsp").forward(req, resp);
    }
}
