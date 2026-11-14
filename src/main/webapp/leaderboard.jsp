<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (request.getAttribute("players") == null) { // opened directly
        response.sendRedirect(request.getContextPath() + "/leaderboard");
        return;
    }
    List<LeaderboardUser> players = (List<LeaderboardUser>) request.getAttribute("players");
    String error = (String) request.getAttribute("error");
    Integer meId = (Integer) session.getAttribute("userId");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Leaderboard | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container">
  <div class="title-row">
    <div>
      <h1 class="page-title">Global Leaderboard</h1>
      <p class="muted">Top players ranked by total XP. Refresh to see the latest standings.</p>
    </div>
    <a class="btn btn-outline" href="<%= ctx %>/leaderboard">&#8635; Refresh</a>
  </div>
<%  if (error != null) { %>
  <div class="alert alert-error"><%= DBConnection.esc(error) %></div>
<%  } %>
<%  if (players.isEmpty()) { %>
  <div class="card center"><p class="muted">No players yet. Be the first to <a href="<%= ctx %>/register.jsp">register</a> and claim the #1 spot!</p></div>
<%  } else { %>
<%      if (players.size() >= 3) {
            LeaderboardUser p1 = players.get(0), p2 = players.get(1), p3 = players.get(2);
%>
  <section class="podium">
    <div class="podium-slot silver"><span class="medal">&#129352;</span><strong><%= DBConnection.esc(p2.getUsername()) %></strong><small><%= p2.getTotalXp() %> XP</small><div class="podium-block">#<%= p2.getRank() %></div></div>
    <div class="podium-slot gold-slot"><span class="medal">&#129351;</span><strong><%= DBConnection.esc(p1.getUsername()) %></strong><small><%= p1.getTotalXp() %> XP</small><div class="podium-block">#<%= p1.getRank() %></div></div>
    <div class="podium-slot bronze"><span class="medal">&#129353;</span><strong><%= DBConnection.esc(p3.getUsername()) %></strong><small><%= p3.getTotalXp() %> XP</small><div class="podium-block">#<%= p3.getRank() %></div></div>
  </section>
<%      } %>
  <div class="table-wrap card">
    <table class="table">
      <thead><tr><th>Rank</th><th>Player</th><th>Level</th><th>Completed</th><th>Total XP</th></tr></thead>
      <tbody>
<%      for (LeaderboardUser u : players) {
            boolean me = meId != null && meId.intValue() == u.getUserId();
            String badge;
            if (u.getRank() == 1) { badge = "<span class=\"rank-badge rank-gold\">&#129351; #1</span>"; }
            else if (u.getRank() == 2) { badge = "<span class=\"rank-badge rank-silver\">&#129352; #2</span>"; }
            else if (u.getRank() == 3) { badge = "<span class=\"rank-badge rank-bronze\">&#129353; #3</span>"; }
            else { badge = "<span class=\"rank-badge\">#" + u.getRank() + "</span>"; }
%>
        <tr class="<%= me ? "row-me" : "" %>">
          <td><%= badge %></td>
          <td><%= DBConnection.esc(u.getUsername()) %><%= me ? " <span class=\"badge badge-cyan\">You</span>" : "" %></td>
          <td>Lv <%= u.getLevel() %> <span class="muted small">(<%= DBConnection.esc(DBConnection.titleForLevel(u.getLevel())) %>)</span></td>
          <td><%= u.getCompleted() %></td>
          <td class="gold"><strong><%= u.getTotalXp() %></strong></td>
        </tr>
<%      } %>
      </tbody>
    </table>
  </div>
<%  } %>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
