import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

/**
 * 推荐引擎 - 基于用户行为统计的动态权重推荐 (支持策略开关)
 */
public class RecommendationEngine {
    private ArrayList<Movie> allMovies;

    public RecommendationEngine(ArrayList<Movie> allMovies) {
        this.allMovies = allMovies;
    }

    /**
     * 获取推荐结果（核心方法）
     * 统一方法名为：getRecommendations
     */
    public ArrayList<MovieScore> getRecommendations(User user, int n, boolean useGenre, boolean useYear, boolean useRating) {
        // 1. 数据准备与去重
        HashSet<String> uniqueIds = new HashSet<>();
        uniqueIds.addAll(user.getWatchedMovieIds());
        uniqueIds.addAll(user.getWatchlist());

        // 如果没有任何数据，回退到高分推荐
        if (uniqueIds.isEmpty()) {
            return getTopRatedMovies(user, n);
        }

        // 2. 计算统计特征
        double genreConcentration = calculateGenreConcentration(uniqueIds);
        double[] yearParams = calculateYearStats(uniqueIds);
        double[] ratingStats = calculateRatingStats(uniqueIds);

        // 3. 动态计算权重
        double[] weights = calculateDynamicWeights(
                genreConcentration,
                yearParams[2], // 方差
                ratingStats[0], // 均值
                ratingStats[1], // 集中度
                useGenre, useYear, useRating
        );
        double w_genre = weights[0];
        double w_year = weights[1];
        double w_rating = weights[2];

        // 打印权重日志，方便调试和展示
        System.out.println("\n[Algorithm Analysis]");
        System.out.println(String.format("   Strategy: Genre=%b | Year=%b | Rating=%b", useGenre, useYear, useRating));
        System.out.println(String.format("   Weights : Genre=%.1f%% | Year=%.1f%% | Rating=%.1f%%",
                w_genre*100, w_year*100, w_rating*100));

        // 4. 计算得分
        ArrayList<MovieScore> movieScores = new ArrayList<>();
        HashMap<String, Double> genrePreference = calculateGenrePreference(uniqueIds);

        for (Movie movie : allMovies) {
            String movieId = movie.getId();
            // 过滤已看和待看
            if (user.getWatchedMovieIds().contains(movieId) || user.getWatchlist().contains(movieId)) {
                continue;
            }

            double score_genre = genrePreference.getOrDefault(movie.getGenre(), 0.0);
            double score_year = Math.exp(-Math.pow(movie.getYear() - yearParams[0], 2) / (2 * Math.pow(yearParams[1], 2)));
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

        // 6. 截取前N个
        ArrayList<MovieScore> result = new ArrayList<>();
        for (int i = 0; i < Math.min(n, movieScores.size()); i++) {
            result.add(movieScores.get(i));
        }
        return result;
    }

    // --- 内部辅助方法 ---

    private double[] calculateDynamicWeights(double genreConc, double yearVar, double ratingMean, double ratingConc,
                                             boolean useGenre, boolean useYear, boolean useRating) {
        double D_genre = 1.0 - genreConc;
        double yearStd = Math.sqrt(yearVar);
        double D_year = Math.min(yearStd / 20.0, 1.0);
        double D_rating = 1.0 - ratingConc;

        double scale = 5.0;
        double exp_genre = useGenre ? Math.exp((1 - D_genre) * scale) : 0.0;
        double exp_year = useYear ? Math.exp((1 - D_year) * scale) : 0.0;
        double exp_rating = useRating ? Math.exp((1 - D_rating) * scale) : 0.0;

        double total = exp_genre + exp_year + exp_rating;
        if (total == 0) return new double[]{0.0, 0.0, 0.0};

        return new double[]{exp_genre/total, exp_year/total, exp_rating/total};
    }

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
        if (std < 3.0) std = 3.0;

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
        for (double r : ratings) sum += r;
        for (double r : ratings) if (r >= 7.0) high++;

        return new double[]{sum / ratings.size(), (double) high / ratings.size()};
    }

    private ArrayList<MovieScore> getTopRatedMovies(User user, int n) {
        ArrayList<MovieScore> list = new ArrayList<>();

        // 1. 筛选出所有没看过的电影
        for (Movie m : allMovies) {
            if (!user.getWatchedMovieIds().contains(m.getId()) && !user.getWatchlist().contains(m.getId())) {
                list.add(new MovieScore(m, m.getRating()/10.0));
            }
        }

        // 2. 先按分数从高到低排序 (保持高质量)
        Collections.sort(list, new Comparator<MovieScore>() {
            @Override
            public int compare(MovieScore m1, MovieScore m2) {
                return Double.compare(m2.score, m1.score);
            }
        });

        // 3. [核心修改] 截取前 50 部作为"精品候选池" (Candidate Pool)
        // 如果想让随机性更大，可以把 50 改成 100；如果想更严谨，改成 20。
        int poolSize = Math.min(list.size(), 20);

        // 创建一个新的列表，只包含前 poolSize 个电影
        ArrayList<MovieScore> candidatePool = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            candidatePool.add(list.get(i));
        }

        // 4. [核心修改] 对精品池进行随机洗牌
        Collections.shuffle(candidatePool);

        // 5. 从洗牌后的池子中取前 N 个返回
        ArrayList<MovieScore> result = new ArrayList<>();
        for(int i = 0; i < Math.min(n, candidatePool.size()); i++) {
            result.add(candidatePool.get(i));
        }

        return result;
    }

    private Movie findMovieById(String id) {
        for (Movie m : allMovies) if (m.getId().equals(id)) return m;
        return null;
    }

    /**
     * 内部类：MovieScore
     * 必须是 public static，否则 Main 无法引用
     */
    public static class MovieScore {
        public Movie movie;
        public double score;
        public MovieScore(Movie movie, double score) {
            this.movie = movie;
            this.score = score;
        }
    }
}