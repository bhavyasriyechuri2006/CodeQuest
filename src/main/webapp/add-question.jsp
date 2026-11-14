<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" import="java.util.*,firstphase.*" %>
<%
    if (session.getAttribute("adminId") == null) {
        response.sendRedirect(request.getContextPath() + "/admin-login.jsp?msg=required");
        return;
    }
    List<String> errors = (List<String>) request.getAttribute("errors");
    String selCat = request.getParameter("category");
    String selDiff = request.getParameter("difficulty");
    String selCorrect = request.getParameter("correctOption");
    String xpValue = request.getParameter("xpReward");
    if (xpValue == null || xpValue.isEmpty()) { xpValue = "50"; }
%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>Add Question | CodeQuest</title>
<link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Orbitron:wght@500;700;900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="/WEB-INF/header.jspf" %>
<main class="container narrow-wide">
  <div class="card form-card">
    <h1 class="page-title">Add Programming Question</h1>
    <p class="muted">The question is stored in MySQL and instantly appears on the students' missions page.</p>
<%  if (errors != null && !errors.isEmpty()) { %>
    <div class="alert alert-error"><ul>
<%      for (String e : errors) { %>
      <li><%= DBConnection.esc(e) %></li>
<%      } %>
    </ul></div>
<%  } %>
    <form action="<%= ctx %>/add-question" method="post">
      <label for="title">Question title</label>
      <input type="text" id="title" name="title" maxlength="100" required value="<%= DBConnection.esc(request.getParameter("title")) %>">

      <label for="description">Description / question text</label>
      <textarea id="description" name="description" rows="4" required><%= DBConnection.esc(request.getParameter("description")) %></textarea>

      <div class="two-col">
        <div>
          <label for="category">Category</label>
          <select id="category" name="category" required>
            <option value="">-- choose --</option>
            <option value="Java" <%= "Java".equals(selCat) ? "selected" : "" %>>Java</option>
            <option value="SQL" <%= "SQL".equals(selCat) ? "selected" : "" %>>SQL</option>
            <option value="DSA" <%= "DSA".equals(selCat) ? "selected" : "" %>>DSA</option>
          </select>
        </div>
        <div>
          <label for="difficulty">Difficulty</label>
          <select id="difficulty" name="difficulty" required>
            <option value="">-- choose --</option>
            <option value="Easy" <%= "Easy".equals(selDiff) ? "selected" : "" %>>Easy</option>
            <option value="Medium" <%= "Medium".equals(selDiff) ? "selected" : "" %>>Medium</option>
            <option value="Hard" <%= "Hard".equals(selDiff) ? "selected" : "" %>>Hard</option>
          </select>
        </div>
      </div>

      <div class="two-col">
        <div><label for="optionA">Option A</label><input type="text" id="optionA" name="optionA" maxlength="255" required value="<%= DBConnection.esc(request.getParameter("optionA")) %>"></div>
        <div><label for="optionB">Option B</label><input type="text" id="optionB" name="optionB" maxlength="255" required value="<%= DBConnection.esc(request.getParameter("optionB")) %>"></div>
        <div><label for="optionC">Option C</label><input type="text" id="optionC" name="optionC" maxlength="255" required value="<%= DBConnection.esc(request.getParameter("optionC")) %>"></div>
        <div><label for="optionD">Option D</label><input type="text" id="optionD" name="optionD" maxlength="255" required value="<%= DBConnection.esc(request.getParameter("optionD")) %>"></div>
      </div>

      <div class="two-col">
        <div>
          <label for="correctOption">Correct answer</label>
          <select id="correctOption" name="correctOption" required>
            <option value="">-- which option is correct? --</option>
            <option value="A" <%= "A".equals(selCorrect) ? "selected" : "" %>>Option A</option>
            <option value="B" <%= "B".equals(selCorrect) ? "selected" : "" %>>Option B</option>
            <option value="C" <%= "C".equals(selCorrect) ? "selected" : "" %>>Option C</option>
            <option value="D" <%= "D".equals(selCorrect) ? "selected" : "" %>>Option D</option>
          </select>
        </div>
        <div>
          <label for="xpReward">XP reward (10 - 500)</label>
          <input type="number" id="xpReward" name="xpReward" min="10" max="500" step="10" required value="<%= DBConnection.esc(xpValue) %>">
        </div>
      </div>

      <div class="btn-row">
        <button type="submit" class="btn btn-primary">Save Question</button>
        <a class="btn btn-outline" href="<%= ctx %>/admin-dashboard.jsp">Cancel</a>
      </div>
    </form>
  </div>
</main>
<footer class="footer">&copy; CodeQuest &mdash; Learn. Solve. Level up.</footer>
</body>
</html>
