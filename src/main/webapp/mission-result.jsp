<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?msg=required");
        return;
    }
    if (request.getAttribute("resMissionTitle") == null) { // opened directly
        response.sendRedirect(request.getContextPath() + "/missions");
        return;
    }
    String rTitle = (String) request.getAttribute("resMissionTitle");
    boolean rCorrect = (Boolean) request.getAttribute("resCorrect");
    int rEarned = (Integer) request.getAttribute("resXpEarned");
    int rMissionXp = (Integer) request.getAttribute("resMissionXp");
    boolean rLevelUp = (Boolean) request.getAttribute("resLeveledUp");
    int rLevel = (Integer) request.getAttribute("resNewLevel");
    String rLevelTitle = (String) request.getAttribute("resNewTitle");
    int rTotalXp = (Integer) request.getAttribute("resTotalXp");
    String rYour = (String) request.getAttribute("resYourAnswer");
    String rCorrectAns = (String) request.getAttribute("resCorrectAnswer");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Mission Result | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container narrow">
<%  if (rLevelUp) { %>
  <div class="levelup-banner">
    <span class="levelup-title">&#127942; LEVEL UP! &#127942;</span>
    <span>You are now Level <strong><%= rLevel %></strong> &mdash; <strong><%= DBConnection.esc(rLevelTitle) %></strong></span>
  </div>
<%  } %>
  <div class="card result-card <%= rCorrect ? "result-correct" : "result-wrong" %>">
    <div class="result-icon"><%= rCorrect ? "&#10004;" : "&#10006;" %></div>
    <h1 class="page-title"><%= rCorrect ? "Correct!" : "Not quite&hellip;" %></h1>
    <p class="muted">Mission: <strong><%= DBConnection.esc(rTitle) %></strong></p>
<%  if (rCorrect) { %>
    <p class="xp-earned">+<%= rEarned %> XP earned</p>
<%  } else { %>
    <p class="xp-earned xp-none">0 XP earned (mission was worth <%= rMissionXp %> XP)</p>
<%  } %>
    <div class="answer-box">
      <p><span class="muted">Your answer:</span> <strong><%= DBConnection.esc(rYour) %></strong></p>
<%  if (!rCorrect) { %>
      <p><span class="muted">Correct answer:</span> <strong class="green"><%= DBConnection.esc(rCorrectAns) %></strong></p>
<%  } %>
    </div>
    <p class="muted">Total XP: <strong class="gold"><%= rTotalXp %></strong> &bull; Level <%= rLevel %> (<%= DBConnection.esc(rLevelTitle) %>)</p>
    <div class="btn-row center-row">
      <a class="btn btn-primary" href="<%= ctx %>/missions">More Missions</a>
      <a class="btn btn-outline" href="<%= ctx %>/dashboard">Dashboard</a>
      <a class="btn btn-outline" href="<%= ctx %>/leaderboard">Leaderboard</a>
    </div>
  </div>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
