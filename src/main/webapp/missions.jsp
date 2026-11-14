<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (session.getAttribute("userId") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp?msg=required");
        return;
    }
    if (request.getAttribute("missions") == null) { // opened directly: go through the servlet
        response.sendRedirect(request.getContextPath() + "/missions");
        return;
    }
    List<Mission> missions = (List<Mission>) request.getAttribute("missions");
    String selCat = (String) request.getAttribute("selectedCategory");
    String selDiff = (String) request.getAttribute("selectedDifficulty");
    int attemptedCount = (Integer) request.getAttribute("attemptedCount");
    String error = (String) request.getAttribute("error");
    String msg = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Missions | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container">
  <h1 class="page-title">Programming Missions</h1>
  <p class="muted">Choose an answer carefully &mdash; you only get <strong>one attempt</strong> per mission.</p>

<%  if ("already".equals(msg)) { %>
  <div class="alert alert-warn">You have already attempted that mission. Each mission allows only one attempt.</div>
<%  } else if ("noanswer".equals(msg)) { %>
  <div class="alert alert-warn">Please select an answer before submitting.</div>
<%  } else if ("invalid".equals(msg) || "notfound".equals(msg)) { %>
  <div class="alert alert-error">That submission was not valid. Please try again.</div>
<%  } else if ("dberror".equals(msg)) { %>
  <div class="alert alert-error">A database error occurred while saving your answer. Nothing was changed; please try again.</div>
<%  } %>
<%  if (error != null) { %>
  <div class="alert alert-error"><%= DBConnection.esc(error) %></div>
<%  } %>

  <form class="filters card" action="<%= ctx %>/missions" method="get">
    <div>
      <label for="category">Category</label>
      <select id="category" name="category">
        <option value="">All categories</option>
        <option value="Java" <%= "Java".equals(selCat) ? "selected" : "" %>>Java</option>
        <option value="SQL" <%= "SQL".equals(selCat) ? "selected" : "" %>>SQL</option>
        <option value="DSA" <%= "DSA".equals(selCat) ? "selected" : "" %>>DSA</option>
      </select>
    </div>
    <div>
      <label for="difficulty">Difficulty</label>
      <select id="difficulty" name="difficulty">
        <option value="">All levels</option>
        <option value="Easy" <%= "Easy".equals(selDiff) ? "selected" : "" %>>Easy</option>
        <option value="Medium" <%= "Medium".equals(selDiff) ? "selected" : "" %>>Medium</option>
        <option value="Hard" <%= "Hard".equals(selDiff) ? "selected" : "" %>>Hard</option>
      </select>
    </div>
    <div class="filter-actions">
      <button type="submit" class="btn btn-primary">Apply Filters</button>
      <a class="btn btn-outline" href="<%= ctx %>/missions">Reset</a>
    </div>
  </form>

  <p class="muted"><%= missions.size() %> mission(s) shown &bull; <%= attemptedCount %> attempted</p>

<%  if (missions.isEmpty()) { %>
  <div class="card center"><p class="muted">No missions match these filters yet. Ask an admin to add more questions!</p></div>
<%  } %>

  <div class="mission-grid">
<%
    for (Mission m : missions) {
        String[] opts = { m.getOptionA(), m.getOptionB(), m.getOptionC(), m.getOptionD() };
        String[] letters = { "A", "B", "C", "D" };
        String diffClass = "badge-green";
        if ("Medium".equals(m.getDifficulty())) { diffClass = "badge-amber"; }
        if ("Hard".equals(m.getDifficulty())) { diffClass = "badge-red"; }
%>
    <article class="card mission <%= m.isAttempted() ? "mission-done" : "" %>">
      <div class="mission-head">
        <span class="badge badge-cyan"><%= DBConnection.esc(m.getCategory()) %></span>
        <span class="badge <%= diffClass %>"><%= DBConnection.esc(m.getDifficulty()) %></span>
        <span class="badge badge-gold">+<%= m.getXpReward() %> XP</span>
<%      if (m.isAttempted()) { %>
        <span class="badge <%= m.isWasCorrect() ? "badge-green" : "badge-red" %>">Attempted &middot; <%= m.isWasCorrect() ? "Correct" : "Incorrect" %></span>
<%      } %>
      </div>
      <h3><%= DBConnection.esc(m.getTitle()) %></h3>
      <p class="mission-desc"><%= DBConnection.esc(m.getDescription()) %></p>
      <form action="<%= ctx %>/submit-answer" method="post">
        <input type="hidden" name="missionId" value="<%= m.getId() %>">
<%      for (int i = 0; i < 4; i++) {
            boolean picked = m.isAttempted() && opts[i] != null && opts[i].equals(m.getUserAnswer());
%>
        <label class="option <%= picked ? "option-picked" : "" %>">
          <input type="radio" name="answer" value="<%= DBConnection.esc(opts[i]) %>" <%= m.isAttempted() ? "disabled" : "required" %> <%= picked ? "checked" : "" %>>
          <span class="option-letter"><%= letters[i] %></span>
          <span><%= DBConnection.esc(opts[i]) %></span>
        </label>
<%      } %>
<%      if (m.isAttempted()) { %>
        <button type="button" class="btn btn-disabled btn-block" disabled>Already attempted</button>
<%      } else { %>
        <button type="submit" class="btn btn-primary btn-block">Submit Answer</button>
<%      } %>
      </form>
    </article>
<%  } %>
  </div>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
