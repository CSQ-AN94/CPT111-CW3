import java.util.ArrayList;

//Manages the user's watchlist
public class Watchlist {
    // Stores movie IDs in the watchlist
    private ArrayList<String> movieIds;

    // Default constructor
    public Watchlist() {
        this.movieIds = new ArrayList<>();
    }

    // Constructor for loading an existing list
    public Watchlist(ArrayList<String> existingList) {
        this.movieIds = existingList != null ? existingList : new ArrayList<>();
    }

    // Add a movie ID if it does not already exist in the list
    public void add(String movieId) {
        if (!movieIds.contains(movieId)) {
            movieIds.add(movieId);
        }
    }

    // Remove a movie ID from the watchlist
    public void remove(String movieId) {
        movieIds.remove(movieId);
    }

    // Return the raw list of movie IDs
    public ArrayList<String> getMovieIds() {
        return movieIds;
    }

    // Return the number of films in the watchlist
    public int size() {
        return movieIds.size();
    }
}
