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
        // When saving to the CSV file:
        // If the password is still in plain text (legacy data),
        // it will be automatically converted into a hashed form.
        if (password == null) return "";
        if (password.startsWith(HASH_PREFIX)) return password;
        return hashWithPrefix(username, password);
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
//    public void setPassword(String password) {
//        this.password = password;
//    }

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
        if (password == null) return inputPassword == null;

        // If the stored password is already hashed,
        // hash the user input and compare the two hash values.
        if (password.startsWith(HASH_PREFIX)) {
            return password.equals(hashWithPrefix(username, inputPassword));
        }

        // Backward compatibility:
        // Allows login using legacy plain-text passwords
        // before they are automatically upgraded to hashed form.
        return password.equals(inputPassword);
    }

    // Return a user summary
    @Override
    public String toString() {
        return "User: " + username + " | Watchlist: " + watchlist.size() +
                " movies | History: " + history.size() + " movies";
    }

    // ---------------- Password hashing helpers (no extra imports required) ----------------
    private static final String HASH_PREFIX = "h$";
    private static final String PEPPER = "CPT111_CW3";
// A constant "pepper" used to slightly strengthen hashing.
// It is hard-coded and is NOT stored in the CSV file.

    // This method is required because MovieAppGUI calls currentUser.setPassword(...)
    public void setPassword(String newPassword) {
        this.password = hashWithPrefix(username, newPassword);
    }

    private String hashWithPrefix(String user, String raw) {
        if (raw == null) raw = "";
        if (user == null) user = "";
        return HASH_PREFIX + fnv1aHex(user + ":" + raw + ":" + PEPPER);
    }

    // FNV-1a 64-bit hash function.
// This is NOT reversible encryption, but one-way hashing.
// It demonstrates that passwords are not stored in plain text
// and does not require any additional Java libraries.
    private String fnv1aHex(String s) {
        long hash = 0xcbf29ce484222325L;
        long prime = 0x100000001b3L;
        for (int i = 0; i < s.length(); i++) {
            hash ^= s.charAt(i);
            hash *= prime;
        }
        return toHex16(hash);
    }

    // Converts a 64-bit hash value into a fixed-length hexadecimal string
    private String toHex16(long v) {
        char[] hex = "0123456789abcdef".toCharArray();
        char[] out = new char[16];
        for (int i = 15; i >= 0; i--) {
            out[i] = hex[(int) (v & 0xF)];
            v >>>= 4;
        }
        return new String(out);
    }

}
