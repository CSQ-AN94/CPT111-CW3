import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

/**
 * 推荐引擎 - 基于用户行为统计的动态权重推荐
 */
public class RecommendationEngine {
    private ArrayList<Movie> allMovies;

    public RecommendationEngine(ArrayList<Movie> allMovies) {
        this.allMovies = allMovies;
    }

    /**
     * 获取推荐结果（包含分数）
     * 返回类型改为 ArrayList<MovieScore> 以便在主程序显示分数
     */
    public ArrayList<MovieScore> getRecommendations(User user, int n) {
        // 1. 数据准备与去重 (Fix: 解决重复计算问题)
        HashSet<String> uniqueIds = new HashSet<>();
        uniqueIds.addAll(user.getWatchedMovieIds());
        uniqueIds.addAll(user.getWatchlist());

        // 如果没有数据，返回高分电影
        if (uniqueIds.isEmpty()) {
            return getTopRatedMovies(user, n);
        }

        // 2. 基于去重后的数据计算统计特征
        // 计算类型集中度
        double genreConcentration = calculateGenreConcentration(uniqueIds);
        // 计算年份方差
        double[] yearParams = calculateYearStats(uniqueIds);
        double yearMean = yearParams[0];
        double yearStd = yearParams[1];
        double yearVariance = yearParams[2];
        // 计算评分集中度
        double[] ratingStats = calculateRatingStats(uniqueIds);
        double ratingMean = ratingStats[0];
        double ratingConcentration = ratingStats[1];

        // 3. 动态计算权重
        double[] weights = calculateDynamicWeights(
                genreConcentration,
                yearVariance,
                ratingMean,
                ratingConcentration
        );
        double w_genre = weights[0];
        double w_year = weights[1];
        double w_rating = weights[2];

        // 打印分析日志（可选，为了展示给老师看算法在运行）
        System.out.println("\n[Algorithm Analysis]");
        System.out.println(String.format("Weights -> Genre: %.1f%% | Year: %.1f%% | Rating: %.1f%%",
                w_genre*100, w_year*100, w_rating*100));

        // 4. 计算每部电影得分
        ArrayList<MovieScore> movieScores = new ArrayList<>();
        HashMap<String, Double> genrePreference = calculateGenrePreference(uniqueIds);

        for (Movie movie : allMovies) {
            String movieId = movie.getId();
            // 过滤掉已看和待看的
            if (user.getWatchedMovieIds().contains(movieId) || user.getWatchlist().contains(movieId)) {
                continue;
            }

            double score_genre = genrePreference.getOrDefault(movie.getGenre(), 0.0);
            double score_year = Math.exp(-Math.pow(movie.getYear() - yearMean, 2) / (2 * Math.pow(yearStd, 2)));
            double score_rating = movie.getRating() / 10.0;

            double finalScore = w_genre * score_genre + w_year * score_year + w_rating * score_rating;
            movieScores.add(new MovieScore(movie, finalScore));
        }

        // 5. 排序
        Collections.sort(movieScores, new Comparator<MovieScore>() {
            @Override
            public int compare(MovieScore ms1, MovieScore ms2) {
                return Double.compare(ms2.score, ms1.score);
            }
        });

        // 6. 返回前N个
        ArrayList<MovieScore> result = new ArrayList<>();
        for (int i = 0; i < Math.min(n, movieScores.size()); i++) {
            result.add(movieScores.get(i));
        }
        return result;
    }

    // --- 辅助统计方法 (修改为接收 HashSet<String>) ---

    private double calculateGenreConcentration(HashSet<String> ids) {
        HashMap<String, Integer> counts = new HashMap<>();
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) counts.put(m.getGenre(), counts.getOrDefault(m.getGenre(), 0) + 1);
        }
        if (ids.isEmpty()) return 0.0;
        double hhi = 0.0;
        for (int count : counts.values()) {
            double p = (double) count / ids.size();
            hhi += p * p;
        }
        return hhi;
    }

    private HashMap<String, Double> calculateGenrePreference(HashSet<String> ids) {
        HashMap<String, Integer> counts = new HashMap<>();
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) counts.put(m.getGenre(), counts.getOrDefault(m.getGenre(), 0) + 1);
        }
        HashMap<String, Double> pref = new HashMap<>();
        int total = ids.size();
        if (total == 0) total = 1;
        for (String g : counts.keySet()) {
            pref.put(g, (double) counts.get(g) / total);
        }
        return pref;
    }

    private double[] calculateYearStats(HashSet<String> ids) {
        ArrayList<Integer> years = new ArrayList<>();
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) years.add(m.getYear());
        }
        if (years.isEmpty()) return new double[]{2000, 10, 100};

        double sum = 0;
        for (int y : years) sum += y;
        double mean = sum / years.size();

        double var = 0;
        for (int y : years) var += Math.pow(y - mean, 2);
        var /= years.size();
        double std = Math.sqrt(var);
        if (std < 3.0) std = 3.0; // 防止除零

        return new double[]{mean, std, var};
    }

    private double[] calculateRatingStats(HashSet<String> ids) {
        ArrayList<Double> ratings = new ArrayList<>();
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) ratings.add(m.getRating());
        }
        if (ratings.isEmpty()) return new double[]{7.0, 0.0};

        double sum = 0;
        int high = 0;
        for (double r : ratings) {
            sum += r;
            if (r >= 7.0) high++;
        }
        return new double[]{sum / ratings.size(), (double) high / ratings.size()};
    }

    private double[] calculateDynamicWeights(double genreConc, double yearVar, double ratingMean, double ratingConc) {
        // 归一化离散度
        double D_genre = 1.0 - genreConc;
        double yearStd = Math.sqrt(yearVar);
        double D_year = Math.min(yearStd / 20.0, 1.0);
        double D_rating = 1.0 - ratingConc;

        // 转化为集中度得分
        double scale = 5.0; // 放大系数
        double exp_genre = Math.exp((1 - D_genre) * scale);
        double exp_year = Math.exp((1 - D_year) * scale);
        double exp_rating = Math.exp((1 - D_rating) * scale);

        double total = exp_genre + exp_year + exp_rating;
        return new double[]{exp_genre/total, exp_year/total, exp_rating/total};
    }

    private ArrayList<MovieScore> getTopRatedMovies(User user, int n) {
        ArrayList<MovieScore> list = new ArrayList<>();
        for (Movie m : allMovies) {
            if (!user.getWatchedMovieIds().contains(m.getId()) && !user.getWatchlist().contains(m.getId())) {
                list.add(new MovieScore(m, m.getRating()/10.0));
            }
        }
        Collections.sort(list, new Comparator<MovieScore>() {
            public int compare(MovieScore m1, MovieScore m2) {
                return Double.compare(m2.score, m1.score);
            }
        });
        ArrayList<MovieScore> result = new ArrayList<>();
        for(int i=0; i<Math.min(n, list.size()); i++) result.add(list.get(i));
        return result;
    }

    private Movie findMovieById(String id) {
        for (Movie m : allMovies) if (m.getId().equals(id)) return m;
        return null;
    }

    // 将内部类改为 public，以便 Main 类可以使用
    public static class MovieScore {
        public Movie movie;
        public double score;
        public MovieScore(Movie movie, double score) {
            this.movie = movie;
            this.score = score;
        }
    }
}