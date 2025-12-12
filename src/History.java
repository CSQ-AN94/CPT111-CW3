import java.util.ArrayList;

// Manages movie watch history entries
public class History {
    private ArrayList<String> entries;

    // Create an empty history list
    public History() {
        this.entries = new ArrayList<>();
    }

    // Create a history list from an existing set of entries
    public History(ArrayList<String> existingEntries) {
        if(existingEntries != null)  this.entries=existingEntries;
        else this.entries = new ArrayList<>();
    }

    // Add a new watch entry if the same movie was not recorded on the same date
    public void add(String movieId, String date) {
        String historyEntry = movieId + "@" + date;

        boolean existsToday = false;
        for (String entry : entries) {
            if (entry.equals(historyEntry)) {
                existsToday = true;
                break;
            }
        }

        // Add entry only if it does not already exist for that date
        if (!existsToday) {
            entries.add(historyEntry);
        }
    }

    // Return the full list of history entries, including dates
    public ArrayList<String> getEntries() {
        return entries;
    }

    // Return only movie IDs from history (dates removed)
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

    // Return the number of history records
    public int size() {
        return entries.size();
    }
}
