<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (session.getAttribute("adminId") != null) {
        response.sendRedirect(request.getContextPath() + "/admin-dashboard.jsp");
        return;
    }
    String msg = request.getParameter("msg");
    String error = (String) request.getAttribute("error");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Admin Login | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container narrow">
  <div class="card form-card">
    <h1 class="page-title">Admin Portal</h1>
    <p class="muted">Restricted area. Administrators only.</p>
<%  if ("loggedout".equals(msg)) { %>
    <div class="alert alert-info">Admin logged out successfully.</div>
<%  } else if ("required".equals(msg)) { %>
    <div class="alert alert-warn">Please log in as an administrator to continue.</div>
<%  } %>
<%  if (error != null) { %>
    <div class="alert alert-error"><%= DBConnection.esc(error) %></div>
<%  } %>
    <form action="<%= ctx %>/admin-login" method="post" autocomplete="off">
      <label for="username">Admin username</label>
      <input type="text" id="username" name="username" required value="<%= DBConnection.esc(request.getParameter("username")) %>">
      <label for="password">Password</label>
      <input type="password" id="password" name="password" required>
      <button type="submit" class="btn btn-primary btn-block">Enter Admin Portal</button>
    </form>
  </div>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
