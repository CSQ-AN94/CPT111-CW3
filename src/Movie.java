// Represent a movie with basic information: ID, title, genre, release year, and rating
public class Movie {
    private String id;
    private String title;
    private String genre;
    private int year;
    private double rating;

    // Create a new Movie with full attribute values
    public Movie(String id, String title, String genre, int year, double rating) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.rating = rating;
    }

    // Return the movie ID
    public String getId() {
        return id;
    }

    // Return the movie title
    public String getTitle() {
        return title;
    }

    // Return the movie genre
    public String getGenre() {
        return genre;
    }

    // Return the release year
    public int getYear() {
        return year;
    }

    // Return the movie rating
    public double getRating() {
        return rating;
    }



    // Return a formatted string describing the movie.
    @Override
    public String toString() {
        return String.format(
                "ID: %s | Title: %s | Genre: %s | Year: %d | Rating: %.1f",
                id, title, genre, year, rating
        );
    }
}
