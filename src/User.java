import java.util.ArrayList;

// Represent a system user with login credentials, a watchlist, and viewing history
public class User {
    //store user details
    private String username;
    private String password;
    private Watchlist watchlist;
    private History history;

    // Create a new user with empty watchlist and history
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.watchlist = new Watchlist();
        this.history = new History();
    }

    // Create a user from saved raw data (used when loading from file)
    public User(String username, String password, ArrayList<String> rawWatchlist, ArrayList<String> rawHistory) {
        this.username = username;
        this.password = password;
        // Wrap raw data into Watchlist and History
        this.watchlist = new Watchlist(rawWatchlist);
        this.history = new History(rawHistory);
    }

    // Return the username
    public String getUsername() {
        return username;
    }

    // Return the stored password
    public String getPassword() {
        return password;
    }

    // Return movie IDs in the watchlist
    public ArrayList<String> getWatchlist() {
        return watchlist.getMovieIds();
    }

    // Return full history entries
    public ArrayList<String> getHistory() {
        return history.getEntries();
    }

    // Return the watched movie IDs
    public ArrayList<String> getWatchedMovieIds() {
        return history.getWatchedMovieIds();
    }

    // Change the password
    public void setPassword(String password) {
        this.password = password;
    }

    // Add a movie to the watchlist
    public void addToWatchlist(String movieId) {
        watchlist.add(movieId);
    }

    // Remove a movie from the watchlist
    public void removeFromWatchlist(String movieId) {
        watchlist.remove(movieId);
    }

    // Add a movie to the viewing history
    public void addToHistory(String movieId, String date) {
        history.add(movieId, date);
    }

    // Check if password matches the stored one
    public boolean verifyPassword(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // Return a user summary
    @Override
    public String toString() {
        return "User: " + username + " | Watchlist: " + watchlist.size() +
                " movies | History: " + history.size() + " movies";
    }
}
