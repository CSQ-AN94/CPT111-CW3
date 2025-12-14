import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Comparator;

/**
 Recommendation Engine - Dynamic Weight-Based Recommendations Using User Behavior Statistics

 This class provides movie recommendations by analyzing the history and preferences of the user
 It supports configurable recommendation strategies through strategy switches (genre, year, rating)
 The algorithm dynamically adjusts weights based on user behavior patterns
 */
public class RecommendationEngine {
    // List of all available movies in the system
    private ArrayList<Movie> allMovies;
    //Constructor
    //List of all movies available for recommendation
    public RecommendationEngine(ArrayList<Movie> allMovies) {
        this.allMovies = allMovies;
    }

    /**
     * Get movie recommendations for a user (Core Method)
     * This method calculates personalized recommendations based on user's viewing history,
     * watchlist, and configurable strategy parameters. It uses dynamic weighting to balance
     * genre preference, year preference, and rating preference
     * @return List of MovieScore objects sorted by recommendation score (highest first)
     */
    public ArrayList<MovieScore> getRecommendations(User user, int n, boolean useGenre, boolean useYear, boolean useRating) {
        // 1. Data preparation and deduplication
        // Combine watched movies and watchlist to get unique movie IDs
        HashSet<String> uniqueIds = new HashSet<>();
        uniqueIds.addAll(user.getWatchedMovieIds());
        uniqueIds.addAll(user.getWatchlist());

        // Fallback: If user has no viewing history, return top-rated movies
        if (uniqueIds.isEmpty()) {
            return getTopRatedMovies(user, n);
        }

        // 2. Calculate statistical features
        double genreConcentration = calculateGenreConcentration(uniqueIds);
        double[] yearParams = calculateYearStats(uniqueIds);
        double[] ratingStats = calculateRatingStats(uniqueIds);

        // 3. Calculate dynamic weights based on user behavior patterns
        double[] weights = calculateDynamicWeights(
                genreConcentration,
                // variance
                yearParams[2],
                // mean rating
                ratingStats[0],
                // high rating concentration
                ratingStats[1],
                useGenre, useYear, useRating
        );
        double w_genre = weights[0];
        double w_year = weights[1];
        double w_rating = weights[2];

        // Log weights for debugging and transparency
        System.out.println("\n[Algorithm Analysis]");
        System.out.println(String.format("   Strategy: Genre=%b | Year=%b | Rating=%b", useGenre, useYear, useRating));
        System.out.println(String.format("   Weights : Genre=%.1f%% | Year=%.1f%% | Rating=%.1f%%",
                w_genre*100, w_year*100, w_rating*100));

        // 4. Calculate recommendation scores for all candidate movies
        ArrayList<MovieScore> movieScores = new ArrayList<>();
        HashMap<String, Double> genrePreference = calculateGenrePreference(uniqueIds);

        for (Movie movie : allMovies) {
            String movieId = movie.getId();
            // Skip movies already watched or in watchlist
            if (user.getWatchedMovieIds().contains(movieId) || user.getWatchlist().contains(movieId)) {
                continue;
            }

            // Calculate component scores
            // Genre score：based on user's genre preference distribution
            double score_genre = genrePreference.getOrDefault(movie.getGenre(), 0.0);
            // Year score: Gaussian distribution centered on user's mean year preference
            double score_year = Math.exp(-Math.pow(movie.getYear() - yearParams[0], 2) / (2 * Math.pow(yearParams[1], 2)));
            // Rating score: normalized movie rating (0-1 scale)
            double score_rating = movie.getRating() / 10.0;

            // Calculate weighted final score
            double finalScore = w_genre * score_genre + w_year * score_year + w_rating * score_rating;
            movieScores.add(new MovieScore(movie, finalScore));
        }

        // 5. Sort movies by score (descending order)
        Collections.sort(movieScores, new Comparator<MovieScore>() {
            @Override
            public int compare(MovieScore ms1, MovieScore ms2) {
                return Double.compare(ms2.score, ms1.score);
            }
        });

        // 6. Return top N recommendations
        ArrayList<MovieScore> result = new ArrayList<>();
        for (int i = 0; i < Math.min(n, movieScores.size()); i++) {
            result.add(movieScores.get(i));
        }
        return result;
    }

    // --- Internal Helper Methods ---

    /**
     * Calculate dynamic weights based on user behavior diversity
     * Uses exponential scaling to emphasize concentrated preferences
     * Higher concentration in a dimension results in higher weight for that dimension
     *
     * @param genreConc Genre concentration (HHI index, 0-1)
     * @param yearVar Variance in year preferences
     * @param ratingMean Average rating of user's watched movies
     * @param ratingConc Concentration of high ratings
     * @param useGenre Enable genre weighting
     * @param useYear Enable year weighting
     * @param useRating Enable rating weighting
     * @return Array of normalized weights [genre_weight, year_weight, rating_weight]
     */
    private double[] calculateDynamicWeights(double genreConc, double yearVar, double ratingMean, double ratingConc,
                                             boolean useGenre, boolean useYear, boolean useRating) {
        // Calculate diversity metrics (inverted concentration)
        double D_genre = 1.0 - genreConc;
        double yearStd = Math.sqrt(yearVar);
        double D_year = Math.min(yearStd / 20.0, 1.0);
        double D_rating = 1.0 - ratingConc;

        // Apply exponential scaling to emphasize strong preferences
        double scale = 5.0;
        // 1. Genre Weight Calculation
        double exp_genre;
        if (useGenre) {
            exp_genre = Math.exp((1 - D_genre) * scale);
        } else {
            exp_genre = 0.0;
        }

        // 2. Year Weight Calculation
        double exp_year;
        if (useYear) {
            exp_year = Math.exp((1 - D_year) * scale);
        } else {
            exp_year = 0.0;
        }

        // 3. Rating Weight Calculation
        double exp_rating;
        if (useRating) {
            exp_rating = Math.exp((1 - D_rating) * scale);
        } else {
            exp_rating = 0.0;
        }

        // Normalize weights to sum to 1.0
        double total = exp_genre + exp_year + exp_rating;
        if (total == 0) return new double[]{0.0, 0.0, 0.0};

        return new double[]{exp_genre/total, exp_year/total, exp_rating/total};
    }

    /**
     * Calculate genre concentration using Herfindahl-Hirschman Index (HHI)
     *
     * HHI measures market concentration. Higher values indicate stronger preference
     * for specific genres. Range: 0 (perfectly diverse) to 1 (single genre only)
     *
     * @param ids Set of movie IDs from user's viewing history
     * @return HHI value (0-1)
     */
    private double calculateGenreConcentration(HashSet<String> ids) {
        HashMap<String, Integer> counts = new HashMap<>();
        // Count occurrences of each genre
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) counts.put(m.getGenre(), counts.getOrDefault(m.getGenre(), 0) + 1);
        }
        if (ids.isEmpty()) return 0.0;

        // Calculate HHI: sum of squared market shares
        double hhi = 0.0;
        for (int count : counts.values()) {
            double p = (double) count / ids.size();
            hhi += p * p;
        }
        return hhi;
    }

    /**
     * Calculate genre preference distribution
     * @param ids Set of movie IDs from user's viewing history
     * @return Map of genre to preference score (proportion of total)
     */
    private HashMap<String, Double> calculateGenrePreference(HashSet<String> ids) {
        HashMap<String, Integer> counts = new HashMap<>();

        // Count genre occurrences
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) counts.put(m.getGenre(), counts.getOrDefault(m.getGenre(), 0) + 1);
        }

        // Convert counts to proportions
        HashMap<String, Double> pref = new HashMap<>();
        int total = ids.size();
        if (total == 0) total = 1;
        for (String g : counts.keySet()) {
            pref.put(g, (double) counts.get(g) / total);
        }
        return pref;
    }

    /**
     * Calculate year statistics from viewing history
     *
     * @param ids Set of movie IDs from user's viewing history
     * @return Array containing [mean, std_dev, variance] of release years
     */
    private double[] calculateYearStats(HashSet<String> ids) {
        ArrayList<Integer> years = new ArrayList<>();

        // Extract years from user's watched movies
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) years.add(m.getYear());
        }

        // Default values if no data available
        if (years.isEmpty()) return new double[]{2000, 10, 100};

        // Calculate mean
        double sum = 0;
        for (int y : years) sum += y;
        double mean = sum / years.size();

        // Calculate variance and standard deviation
        double var = 0;
        for (int y : years) var += Math.pow(y - mean, 2);
        var /= years.size();
        double std = Math.sqrt(var);

        // Minimum standard deviation to prevent over-fitting
        if (std < 3.0) std = 3.0;

        return new double[]{mean, std, var};
    }

    /**
     * Calculate rating statistics from viewing history
     *
     * @param ids Set of movie IDs from user's viewing history
     * @return Array containing [mean_rating, high_rating_concentration]
     */
    private double[] calculateRatingStats(HashSet<String> ids) {
        ArrayList<Double> ratings = new ArrayList<>();

        // Extract ratings from user's watched movies
        for (String id : ids) {
            Movie m = findMovieById(id);
            if (m != null) ratings.add(m.getRating());
        }

        // Default values if no data available
        if (ratings.isEmpty()) return new double[]{7.0, 0.0};

        // Calculate mean rating
        double sum = 0;
        int high = 0;
        for (double r : ratings) sum += r;
        for (double r : ratings) if (r >= 7.0) high++;

        // Return [mean, proportion of high ratings]
        return new double[]{sum / ratings.size(), (double) high / ratings.size()};
    }

    /**
     * Fallback recommendation method for users with no viewing history
     * Returns top-rated movies with randomization to ensure variety
     * Creates a candidate pool of high-quality movies, shuffles them,
     * and returns a random subset
     *
     * @return List of MovieScore objects for top-rated movies
     */
    private ArrayList<MovieScore> getTopRatedMovies(User user, int n) {
        ArrayList<MovieScore> list = new ArrayList<>();

        // 1. Filter unwatched movies
        for (Movie m : allMovies) {
            if (!user.getWatchedMovieIds().contains(m.getId()) && !user.getWatchlist().contains(m.getId())) {
                list.add(new MovieScore(m, m.getRating()/10.0));
            }
        }

        // 2. Sort by rating (descending) to maintain quality
        Collections.sort(list, new Comparator<MovieScore>() {
            @Override
            public int compare(MovieScore m1, MovieScore m2) {
                return Double.compare(m2.score, m1.score);
            }
        });

        // 3. Create a high-quality candidate pool
        // Pool size can be adjusted: larger pool = more randomness, smaller pool = stricter quality
        int poolSize = Math.min(list.size(), 20);

        // Extract top poolSize movies as candidates
        ArrayList<MovieScore> candidatePool = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            candidatePool.add(list.get(i));
        }

        // 4. Shuffle the candidate pool to add randomization
        // This prevents always recommending the exact same movies to new users
        Collections.shuffle(candidatePool);

        // 5. Return top N from shuffled pool
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
     * Inner Class: MovieScore
     * Represents a movie with its calculated recommendation score
     * Must be public static to be accessible from outside classes (e.g., Main)
     */
    public static class MovieScore {
        public Movie movie;
        public double score;
        /**
         * Constructor
         * @param score The calculated recommendation score
         */
        public MovieScore(Movie movie, double score) {
            this.movie = movie;
            this.score = score;
        }
    }
}