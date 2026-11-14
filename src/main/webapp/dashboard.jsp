<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?msg=required");
        return;
    }
    if (request.getAttribute("dashUsername") == null) { // opened directly: go through the servlet
        response.sendRedirect(request.getContextPath() + "/dashboard");
        return;
    }
    String dUser = (String) request.getAttribute("dashUsername");
    int dXp = (Integer) request.getAttribute("dashTotalXp");
    int dLevel = (Integer) request.getAttribute("dashLevel");
    String dTitle = (String) request.getAttribute("dashLevelTitle");
    String dNext = (String) request.getAttribute("dashNextTitle");
    int dCompleted = (Integer) request.getAttribute("dashCompleted");
    int dAttempted = (Integer) request.getAttribute("dashAttempted");
    int dTotal = (Integer) request.getAttribute("dashTotalMissions");
    int dInto = (Integer) request.getAttribute("dashXpIntoLevel");
    int dPer = (Integer) request.getAttribute("dashXpPerLevel");
    int dProgress = (Integer) request.getAttribute("dashProgress");
    boolean dMax = (Boolean) request.getAttribute("dashMaxLevel");
    String dError = (String) request.getAttribute("dashError");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Dashboard | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container">
<%  if (dError != null) { %>
  <div class="alert alert-error"><%= DBConnection.esc(dError) %></div>
<%  } %>
  <section class="card hero-card">
    <div>
      <p class="eyebrow">Player Dashboard</p>
      <h1 class="page-title">Welcome, <span class="accent"><%= DBConnection.esc(dUser) %></span></h1>
      <p class="muted">Rank title: <span class="badge badge-purple"><%= DBConnection.esc(dTitle) %></span></p>
    </div>
    <div class="level-orb"><small>LEVEL</small><strong><%= dLevel %></strong></div>
  </section>

  <section class="stats-grid">
    <div class="card stat"><span class="stat-label">Total XP</span><span class="stat-value gold"><%= dXp %></span></div>
    <div class="card stat"><span class="stat-label">Current Level</span><span class="stat-value cyan"><%= dLevel %></span></div>
    <div class="card stat"><span class="stat-label">Missions Completed</span><span class="stat-value green"><%= dCompleted %> <small>/ <%= dTotal %></small></span></div>
    <div class="card stat"><span class="stat-label">Attempts Used</span><span class="stat-value"><%= dAttempted %></span></div>
  </section>

  <section class="card">
    <h2>Progress to next level</h2>
<%  if (dMax) { %>
    <p class="muted">You reached the maximum level. You are a <strong><%= DBConnection.esc(dTitle) %></strong>!</p>
<%  } else { %>
    <p class="muted"><%= dInto %> / <%= dPer %> XP in this level &mdash; next title: <strong><%= DBConnection.esc(dNext) %></strong></p>
<%  } %>
    <div class="progress"><div class="progress-bar" style="width: <%= dProgress %>%"></div></div>
    <p class="progress-label"><%= dProgress %>% complete</p>
    <div class="btn-row">
      <a class="btn btn-primary" href="<%= ctx %>/missions">Play Missions</a>
      <a class="btn btn-outline" href="<%= ctx %>/leaderboard">View Leaderboard</a>
    </div>
  </section>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
