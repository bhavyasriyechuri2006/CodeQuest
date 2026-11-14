<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Register | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container narrow">
  <div class="card form-card">
    <h1 class="page-title">Create Your Player</h1>
    <p class="muted">Join CodeQuest, start at Level 1 with 0 XP and climb the leaderboard.</p>
<%
    List<String> errors = (List<String>) request.getAttribute("errors");
    if (errors != null && !errors.isEmpty()) {
%>
    <div class="alert alert-error"><ul>
<%      for (String e : errors) { %>
      <li><%= DBConnection.esc(e) %></li>
<%      } %>
    </ul></div>
<%  } %>
    <form action="<%= ctx %>/register" method="post" autocomplete="off">
      <label for="username">Username</label>
      <input type="text" id="username" name="username" maxlength="30" required value="<%= DBConnection.esc(request.getParameter("username")) %>" placeholder="e.g. code_ninja">
      <label for="email">Email address</label>
      <input type="email" id="email" name="email" maxlength="100" required value="<%= DBConnection.esc(request.getParameter("email")) %>" placeholder="you@example.com">
      <label for="password">Password</label>
      <input type="password" id="password" name="password" minlength="6" required placeholder="At least 6 characters">
      <label for="confirmPassword">Confirm password</label>
      <input type="password" id="confirmPassword" name="confirmPassword" minlength="6" required placeholder="Repeat password">
      <button type="submit" class="btn btn-primary btn-block">Register</button>
    </form>
    <p class="muted center">Already a player? <a href="<%= ctx %>/login.jsp">Login here</a></p>
  </div>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
