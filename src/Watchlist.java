import java.util.ArrayList;

/**
 * Watchlist类 - 封装待看列表的逻辑
 * 符合 Spec 要求：必须实现 Watchlist 类
 */
public class Watchlist {
    private ArrayList<String> movieIds;

    // 默认构造函数
    public Watchlist() {
        this.movieIds = new ArrayList<>();
    }

    // 从已有列表初始化的构造函数（用于文件加载）
    public Watchlist(ArrayList<String> existingList) {
        this.movieIds = existingList != null ? existingList : new ArrayList<>();
    }

    /**
     * 添加电影到待看列表
     */
    public void add(String movieId) {
        if (!movieIds.contains(movieId)) {
            movieIds.add(movieId);
        }
    }

    /**
     * 从待看列表移除
     */
    public void remove(String movieId) {
        movieIds.remove(movieId);
    }

    /**
     * 检查是否包含
     */
    public boolean contains(String movieId) {
        return movieIds.contains(movieId);
    }

    /**
     * 获取原始列表数据（用于兼容 UserFileHandler 和遍历显示）
     */
    public ArrayList<String> getMovieIds() {
        return movieIds;
    }

    public int size() {
        return movieIds.size();
    }
}