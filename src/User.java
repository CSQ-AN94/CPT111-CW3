import java.util.ArrayList;

/**
 * User类 - 表示一个用户
 * 包含用户名、密码、观看列表和观看历史
 */
public class User {
    private String username;
    private String password;
    private ArrayList<String> watchlist;  // 改为String类型
    private ArrayList<String> history;    // 改为String类型（包含日期信息）

    // 构造函数
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.watchlist = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    // 带watchlist和history的构造函数（用于从文件加载）
    public User(String username, String password, ArrayList<String> watchlist, ArrayList<String> history) {
        this.username = username;
        this.password = password;
        this.watchlist = watchlist;
        this.history = history;
    }

    // Getter方法
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getWatchlist() {
        return watchlist;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    // Setter方法
    public void setPassword(String password) {
        this.password = password;
    }

    // 添加电影到观看列表
    public void addToWatchlist(String movieId) {
        if (!watchlist.contains(movieId)) {
            watchlist.add(movieId);
        }
    }

    // 从观看列表移除电影
    public void removeFromWatchlist(String movieId) {
        watchlist.remove(movieId);
    }

    // 添加电影到观看历史（带日期）
    public void addToHistory(String movieId, String date) {
        String historyEntry = movieId + "@" + date;
        // 避免同一天重复添加
        boolean existsToday = false;
        for (String entry : history) {
            if (entry.equals(historyEntry)) {
                existsToday = true;
                break;
            }
        }
        if (!existsToday) {
            history.add(historyEntry);
        }
        // 注意：不再自动从watchlist中删除，允许"二刷"
        // 用户可以手动管理watchlist
    }

    // 获取用户观看过的所有电影ID（不含日期）
    public ArrayList<String> getWatchedMovieIds() {
        ArrayList<String> movieIds = new ArrayList<>();
        for (String entry : history) {
            String[] parts = entry.split("@");
            if (parts.length > 0) {
                movieIds.add(parts[0]);
            }
        }
        return movieIds;
    }

    // 验证密码
    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    @Override
    public String toString() {
        return "User: " + username + " | Watchlist: " + watchlist.size() +
                " movies | History: " + history.size() + " movies";
    }
}