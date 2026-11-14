package firstphase;

/** Model bean for one row of the leaderboard. */
public class LeaderboardUser {
    private int rank;
    private int userId;
    private String username;
    private int level;
    private int completed;
    private int totalXp;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getCompleted() { return completed; }
    public void setCompleted(int completed) { this.completed = completed; }
    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }
}
