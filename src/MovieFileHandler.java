import java.io.*;
import java.util.ArrayList;

// Handle loading and searching movies from a CSV file
public class MovieFileHandler {

    // Load movie data from a CSV file and returns a list of Movie objects
    // CSV format per line: ID,Title,Genre,Year,Rating
    public ArrayList<Movie> loadMovies(String filename) {
        ArrayList<Movie> movies = new ArrayList<>();
        BufferedReader br = null;

        try {
            // Open the CSV file for reading
            br = new BufferedReader(new FileReader(filename));
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // Skip the header (first line)
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Ignore empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    // Split CSV line by comma
                    String[] data = line.split(",");

                    if (data.length >= 5) {
                        String id = data[0].trim();
                        String title = data[1].trim();
                        String genre = data[2].trim();
                        int year = Integer.parseInt(data[3].trim());
                        double rating = Double.parseDouble(data[4].trim());

                        // Create a Movie object and add it to the list
                        Movie movie = new Movie(id, title, genre, year, rating);
                        movies.add(movie);
                    }
                }
                // Handle invalid number format in year or rating
                catch (NumberFormatException e) {
                    System.out.println("Warning: Invalid data format in line: " + line);
                }
            }

            System.out.println("Successfully loaded " + movies.size() + " movies.");

        } catch (FileNotFoundException e) {
            // File not found error
            System.out.println("Error: Movies file not found - " + filename);
        } catch (IOException e) {
            // General reading error
            System.out.println("Error reading movies file: " + e.getMessage());
        } finally {
            // Close file stream safely
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Error closing file: " + e.getMessage());
                }
            }
        }

        return movies;
    }

    // Search for a movie in the list by its ID
    // Return the matching Movie object or null if not found
    public Movie findMovieById(ArrayList<Movie> movies, String id) {
        for (Movie movie : movies) {
            if (movie.getId().equals(id)) {
                return movie;
            }
        }
        return null;
    }
}