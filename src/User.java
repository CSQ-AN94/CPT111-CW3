import java.util.ArrayList;

/**
 * User类 - 表示一个用户
 * 更新：使用 Watchlist 和 History 对象组合，符合 OO 设计要求
 */
public class User {
    private String username;
    private String password;

    // 使用自定义类，而不是直接使用 ArrayList
    private Watchlist watchlist;
    private History history;

    // 构造函数 (注册新用户)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.watchlist = new Watchlist();
        this.history = new History();
    }

    // 构造函数 (从文件加载)
    // UserFileHandler 传递过来的是 ArrayList，我们在这里将它们包装进对象
    public User(String username, String password, ArrayList<String> rawWatchlist, ArrayList<String> rawHistory) {
        this.username = username;
        this.password = password;
        this.watchlist = new Watchlist(rawWatchlist);
        this.history = new History(rawHistory);
    }

    // Getter方法
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // --- 关键修改：为了兼容其他类，这里返回内部的数据 ---

    // 获取观看列表数据
    public ArrayList<String> getWatchlist() {
        return watchlist.getMovieIds();
    }

    // 获取历史记录数据 (ID@Date)
    public ArrayList<String> getHistory() {
        return history.getEntries();
    }

    // 获取仅含ID的历史记录 (用于算法)
    public ArrayList<String> getWatchedMovieIds() {
        return history.getWatchedMovieIds();
    }

    // Setter方法
    public void setPassword(String password) {
        this.password = password;
    }

    // --- 业务逻辑：委托给子模块处理 ---

    // 添加电影到观看列表
    public void addToWatchlist(String movieId) {
        watchlist.add(movieId);
    }

    // 从观看列表移除电影
    public void removeFromWatchlist(String movieId) {
        watchlist.remove(movieId);
    }

    // 添加电影到观看历史（带日期）
    public void addToHistory(String movieId, String date) {
        history.add(movieId, date);
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