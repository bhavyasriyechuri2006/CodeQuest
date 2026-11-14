# CodeQuest - Programming Learning Game (Jakarta Servlets + JSP + JDBC + MySQL)

## Requirements
- Eclipse IDE for Enterprise Java and Web Developers, JDK 17, Apache Tomcat 10.1, MySQL Server running on localhost:3306
- mysql-connector-j 8.x JAR  ->  copy into `src/main/webapp/WEB-INF/lib/`

## Setup (5 steps)
1. Eclipse: **File > Import > General > Existing Projects into Workspace** and select the `CodeQuest` folder.
2. Make sure a server runtime named **Apache Tomcat v10.1** exists (Window > Preferences > Server > Runtime Environments).
   If your runtime has another name: right-click project > Properties > Targeted Runtimes, tick your Tomcat 10.1.
3. Copy the MySQL connector JAR into `WEB-INF/lib`, then right-click project > Refresh.
4. If your MySQL root password is not `root`, edit `DB_PASSWORD` in `firstphase/DBConnection.java`.
5. Right-click project > **Run As > Run on Server**, open http://localhost:8080/CodeQuest/

The database `codequest_db`, all 4 tables, the default admin and 6 starter questions are created automatically on first use.

## Default admin
Username `admin` / Password `admin123` (portal: Admin Login in the navbar). Passwords are stored as SHA-256 hashes.

## Game rules
- Level = (total XP / 200) + 1, max level 10
- One attempt per mission (wrong answers also use the attempt, 0 XP)
- Titles: 1 Beginner Coder, 2 Script Kiddie, 3 Junior Developer, 4 Code Warrior, 5 Debug Master, 6-7 Algorithm Ninja, 8+ Code Legend

## Where the required concepts are used
- **Cookies**: LoginServlet writes `rememberedUser`; login.jsp reads it
- **Session + invalidation**: LoginServlet / LogoutServlet; separate admin attributes in AdminLoginServlet / AdminLogoutServlet
- **ServletConfig**: AdminLoginServlet init-params in web.xml
- **ServletContext**: appName context-param and shared admin-login counter
- **RequestDispatcher.forward**: every servlet -> JSP data hand-off; **sendRedirect**: navigation after login/register/logout
- **JDBC transaction**: SubmitAnswerServlet (setAutoCommit(false), commit / rollback)
