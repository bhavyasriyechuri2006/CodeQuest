<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Login | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<%
    // Cookie: read the "remembered user" cookie written by LoginServlet
    String remembered = "";
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
        for (Cookie ck : cookies) {
            if ("rememberedUser".equals(ck.getName())) {
                remembered = ck.getValue();
            }
        }
    }
    String identifierValue = request.getParameter("identifier");
    if (identifierValue == null) {
        identifierValue = remembered;
    }
    boolean rememberChecked = !remembered.isEmpty() || request.getParameter("identifier") == null;
    String msg = request.getParameter("msg");
    String error = (String) request.getAttribute("error");
%>
<main class="container narrow">
  <div class="card form-card">
    <h1 class="page-title">Student Login</h1>
    <p class="muted">Welcome back, player. Log in with your username or email.</p>
<%  if ("registered".equals(msg)) { %>
    <div class="alert alert-success">Registration successful! Please log in.</div>
<%  } else if ("loggedout".equals(msg)) { %>
    <div class="alert alert-info">You have been logged out safely.</div>
<%  } else if ("required".equals(msg)) { %>
    <div class="alert alert-warn">Please log in to access that page.</div>
<%  } %>
<%  if (error != null) { %>
    <div class="alert alert-error"><%= DBConnection.esc(error) %></div>
<%  } %>
    <form action="<%= ctx %>/login" method="post">
      <label for="identifier">Username or email</label>
      <input type="text" id="identifier" name="identifier" required value="<%= DBConnection.esc(identifierValue) %>" placeholder="username or email">
      <label for="password">Password</label>
      <input type="password" id="password" name="password" required placeholder="Your password">
      <label class="check"><input type="checkbox" name="remember" <%= rememberChecked ? "checked" : "" %>> Remember my username</label>
      <button type="submit" class="btn btn-primary btn-block">Login</button>
    </form>
    <p class="muted center">New here? <a href="<%= ctx %>/register.jsp">Create an account</a></p>
  </div>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
