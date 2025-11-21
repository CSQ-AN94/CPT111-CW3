import java.io.*;
import java.util.ArrayList;

/**
 * MovieFileHandler类 - 处理电影数据的文件读写
 */
public class MovieFileHandler {

    /**
     * 从CSV文件加载电影数据
     * @param filename 文件路径
     * @return 电影列表
     */
    public ArrayList<Movie> loadMovies(String filename) {
        ArrayList<Movie> movies = new ArrayList<>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                // 跳过标题行
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // 处理空行
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    // 按逗号分割
                    String[] data = line.split(",");

                    if (data.length >= 5) {
                        String id = data[0].trim();  // 改为String
                        String title = data[1].trim();
                        String genre = data[2].trim();
                        int year = Integer.parseInt(data[3].trim());
                        double rating = Double.parseDouble(data[4].trim());

                        Movie movie = new Movie(id, title, genre, year, rating);
                        movies.add(movie);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Warning: Invalid data format in line: " + line);
                }
            }

            System.out.println("Successfully loaded " + movies.size() + " movies.");

        } catch (FileNotFoundException e) {
            System.out.println("Error: Movies file not found - " + filename);
        } catch (IOException e) {
            System.out.println("Error reading movies file: " + e.getMessage());
        } finally {
            // 关闭文件流
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

    /**
     * 根据ID查找电影
     * @param movies 电影列表
     * @param id 电影ID
     * @return 找到的电影，如果没找到返回null
     */
    public Movie findMovieById(ArrayList<Movie> movies, String id) {
        for (Movie movie : movies) {
            if (movie.getId().equals(id)) {
                return movie;
            }
        }
        return null;
    }
}