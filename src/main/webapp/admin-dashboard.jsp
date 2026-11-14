<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (session.getAttribute("adminId") == null) {
        response.sendRedirect(request.getContextPath() + "/admin-login.jsp?msg=required");
        return;
    }
    // ServletContext (implicit object "application"): context-param + shared counter
    String appName = application.getInitParameter("appName");
    Integer adminLogins = (Integer) application.getAttribute("adminLoginCount");
    String portalTitle = (String) session.getAttribute("adminPortalTitle");
    Object timeout = session.getAttribute("adminTimeoutMinutes");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Admin Dashboard | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container">
  <section class="card hero-card">
    <div>
      <p class="eyebrow"><%= DBConnection.esc(portalTitle != null ? portalTitle : appName) %></p>
      <h1 class="page-title">Welcome, <span class="accent"><%= DBConnection.esc((String) session.getAttribute("adminUsername")) %></span></h1>
      <p class="muted">Manage the <strong><%= DBConnection.esc(appName) %></strong> question bank.</p>
    </div>
  </section>

  <section class="stats-grid">
    <div class="card stat"><span class="stat-label">Application (ServletContext)</span><span class="stat-value cyan small-value"><%= DBConnection.esc(appName) %></span></div>
    <div class="card stat"><span class="stat-label">Admin logins since server start</span><span class="stat-value gold"><%= adminLogins == null ? 0 : adminLogins %></span></div>
    <div class="card stat"><span class="stat-label">Admin session timeout (ServletConfig)</span><span class="stat-value green"><%= timeout == null ? "-" : timeout %> min</span></div>
  </section>

  <section class="action-grid">
    <a class="card action" href="<%= ctx %>/add-question"><span class="action-icon">&#10133;</span><h3>Add Question</h3><p class="muted">Create a new Java, SQL or DSA mission.</p></a>
    <a class="card action" href="<%= ctx %>/view-questions"><span class="action-icon">&#128203;</span><h3>View Questions</h3><p class="muted">Browse every question in the database.</p></a>
    <a class="card action" href="<%= ctx %>/admin-logout"><span class="action-icon">&#128682;</span><h3>Logout</h3><p class="muted">End the admin session securely.</p></a>
  </section>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
