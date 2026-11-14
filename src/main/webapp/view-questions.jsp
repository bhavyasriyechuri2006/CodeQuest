<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (session.getAttribute("adminId") == null) {
        response.sendRedirect(request.getContextPath() + "/admin-login.jsp?msg=required");
        return;
    }
    if (request.getAttribute("questions") == null) { // opened directly
        response.sendRedirect(request.getContextPath() + "/view-questions");
        return;
    }
    List<Mission> questions = (List<Mission>) request.getAttribute("questions");
    String error = (String) request.getAttribute("error");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>View Questions | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container">
  <div class="title-row">
    <div>
      <h1 class="page-title">All Questions</h1>
      <p class="muted"><%= questions.size() %> question(s) stored in the database.</p>
    </div>
    <div class="btn-row">
      <a class="btn btn-primary" href="<%= ctx %>/add-question">+ Add Question</a>
      <a class="btn btn-outline" href="<%= ctx %>/admin-dashboard.jsp">Admin Home</a>
    </div>
  </div>
<%  if ("added".equals(msg)) { %>
  <div class="alert alert-success">Question added successfully!</div>
<%  } %>
<%  if (error != null) { %>
  <div class="alert alert-error"><%= DBConnection.esc(error) %></div>
<%  } %>
<%  if (questions.isEmpty()) { %>
  <div class="card center"><p class="muted">No questions yet. Add your first one!</p></div>
<%  } else { %>
  <div class="table-wrap card">
    <table class="table">
      <thead><tr><th>#</th><th>Title</th><th>Category</th><th>Difficulty</th><th>XP Reward</th></tr></thead>
      <tbody>
<%      for (Mission q : questions) {
            String diffClass = "badge-green";
            if ("Medium".equals(q.getDifficulty())) { diffClass = "badge-amber"; }
            if ("Hard".equals(q.getDifficulty())) { diffClass = "badge-red"; }
%>
        <tr>
          <td><%= q.getId() %></td>
          <td><%= DBConnection.esc(q.getTitle()) %></td>
          <td><span class="badge badge-cyan"><%= DBConnection.esc(q.getCategory()) %></span></td>
          <td><span class="badge <%= diffClass %>"><%= DBConnection.esc(q.getDifficulty()) %></span></td>
          <td class="gold"><strong><%= q.getXpReward() %></strong></td>
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
