import java.util.ArrayList;
import java.util.HashMap;

/**
 * RecommendationEngine类 - 基于用户观看历史推荐电影
 */
public class RecommendationEngine {
    private ArrayList<Movie> allMovies;

    public RecommendationEngine(ArrayList<Movie> allMovies) {
        this.allMovies = allMovies;
    }

    /**
     * 为用户推荐电影（基于最常看的类型）
     * @param user 用户
     * @param n 推荐数量
     * @return 推荐的电影列表
     */
    public ArrayList<Movie> getRecommendations(User user, int n) {
        ArrayList<Movie> recommendations = new ArrayList<>();

        // 1. 找出用户最常看的类型
        String favoriteGenre = getMostWatchedGenre(user);

        if (favoriteGenre == null) {
            // 如果用户没有观看历史，推荐高评分电影
            return getTopRatedMovies(user, n);
        }

        // 2. 找出该类型的电影，且用户没看过、不在观看列表中
        ArrayList<String> watchedIds = user.getWatchedMovieIds();
        for (Movie movie : allMovies) {
            if (movie.getGenre().equals(favoriteGenre) &&
                    !watchedIds.contains(movie.getId()) &&
                    !user.getWatchlist().contains(movie.getId())) {
                recommendations.add(movie);

                if (recommendations.size() >= n) {
                    break;
                }
            }
        }

        // 3. 如果同类型电影不够，补充其他高评分电影
        if (recommendations.size() < n) {
            for (Movie movie : allMovies) {
                if (!watchedIds.contains(movie.getId()) &&
                        !user.getWatchlist().contains(movie.getId()) &&
                        !recommendations.contains(movie)) {
                    recommendations.add(movie);

                    if (recommendations.size() >= n) {
                        break;
                    }
                }
            }
        }

        return recommendations;
    }

    /**
     * 获取用户最常看的电影类型
     * @param user 用户
     * @return 最常看的类型
     */
    private String getMostWatchedGenre(User user) {
        HashMap<String, Integer> genreCount = new HashMap<>();

        // 统计每个类型的观看次数（从history中提取电影ID）
        ArrayList<String> watchedIds = user.getWatchedMovieIds();
        for (String movieId : watchedIds) {
            Movie movie = findMovieById(movieId);
            if (movie != null) {
                String genre = movie.getGenre();
                genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
            }
        }

        // 也考虑观看列表中的电影类型
        for (String movieId : user.getWatchlist()) {
            Movie movie = findMovieById(movieId);
            if (movie != null) {
                String genre = movie.getGenre();
                genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
            }
        }

        // 找出出现次数最多的类型
        String mostWatchedGenre = null;
        int maxCount = 0;

        for (String genre : genreCount.keySet()) {
            if (genreCount.get(genre) > maxCount) {
                maxCount = genreCount.get(genre);
                mostWatchedGenre = genre;
            }
        }

        return mostWatchedGenre;
    }

    /**
     * 推荐高评分电影（当用户没有观看历史时）
     * @param user 用户
     * @param n 推荐数量
     * @return 高评分电影列表
     */
    private ArrayList<Movie> getTopRatedMovies(User user, int n) {
        ArrayList<Movie> topRated = new ArrayList<>();
        ArrayList<String> watchedIds = user.getWatchedMovieIds();

        // 简单地按顺序选择高评分电影
        for (Movie movie : allMovies) {
            if (movie.getRating() >= 7.0 &&
                    !watchedIds.contains(movie.getId()) &&
                    !user.getWatchlist().contains(movie.getId())) {
                topRated.add(movie);

                if (topRated.size() >= n) {
                    break;
                }
            }
        }

        return topRated;
    }

    /**
     * 根据ID查找电影
     * @param id 电影ID
     * @return 电影对象
     */
    private Movie findMovieById(String id) {
        for (Movie movie : allMovies) {
            if (movie.getId().equals(id)) {
                return movie;
            }
        }
        return null;
    }
}