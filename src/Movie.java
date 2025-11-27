/**
 * Movie类 - 表示一部电影
 * 包含电影的基本信息：ID、标题、类型、年份、评分
 */
public class Movie {
    private String id;
    private String title;
    private String genre;
    private int year;
    private double rating;

    // 构造函数
    public Movie(String id, String title, String genre, int year, double rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
    }

    // Getter方法
    public String getId() {  // 改为返回String
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public int getYear() {
        return year;
    }

    public double getRating() {
        return rating;
    }

    // Setter方法
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    // 重写toString方法，方便打印电影信息
    @Override
    public String toString() {
        return String.format("ID: %s | Title: %s | Genre: %s | Year: %d | Rating: %.1f",
                id, title, genre, year, rating);
    }
}