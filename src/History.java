import java.util.ArrayList;

/**
 * History类 - 封装观看历史的逻辑
 * 符合 Spec 要求：必须实现 History 类
 */
public class History {
    private ArrayList<String> entries; // 存储格式: "ID@Date"

    // 默认构造函数
    public History() {
        this.entries = new ArrayList<>();
    }

    // 从已有列表初始化的构造函数
    public History(ArrayList<String> existingEntries) {
        this.entries = existingEntries != null ? existingEntries : new ArrayList<>();
    }

    /**
     * 添加观看记录
     * @param movieId 电影ID
     * @param date 日期字符串
     */
    public void add(String movieId, String date) {
        String historyEntry = movieId + "@" + date;

        // 逻辑迁移：检查今天是否重复添加
        boolean existsToday = false;
        for (String entry : entries) {
            if (entry.equals(historyEntry)) {
                existsToday = true;
                break;
            }
        }

        if (!existsToday) {
            entries.add(historyEntry);
        }
    }

    /**
     * 获取完整的历史记录字符串列表 (ID@Date)
     * 用于文件保存和带日期的显示
     */
    public ArrayList<String> getEntries() {
        return entries;
    }

    /**
     * 只获取看过的电影ID（不含日期）
     * 用于推荐算法去重
     */
    public ArrayList<String> getWatchedMovieIds() {
        ArrayList<String> movieIds = new ArrayList<>();
        for (String entry : entries) {
            String[] parts = entry.split("@");
            if (parts.length > 0) {
                movieIds.add(parts[0]);
            }
        }
        return movieIds;
    }

    public int size() {
        return entries.size();
    }
}