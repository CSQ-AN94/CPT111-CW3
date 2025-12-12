import java.io.*;
import java.util.ArrayList;

// Handle loading and saving user data to/from a CSV file
public class UserFileHandler {

    // Load all users from the given CSV file
    public ArrayList<User> loadUsers(String filename) {
        ArrayList<User> users = new ArrayList<>();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(filename));
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {

                // Skip the header row
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] data = line.split(",");

                    // Ensure username and password exist
                    if (data.length >= 2) {
                        String username = data[0].trim();
                        String password = data[1].trim();

                        // Parse watchlist field (e.g., M008;M015)
                        ArrayList<String> watchlist = new ArrayList<>();
                        if (data.length > 2 && !data[2].trim().isEmpty()) {
                            String[] watchIds = data[2].split(";");
                            for (String id : watchIds) {
                                watchlist.add(id.trim());
                            }
                        }

                        // Parse history field (e.g., M001@2025-07-12;M011@2025-08-10)
                        ArrayList<String> history = new ArrayList<>();
                        if (data.length > 3 && !data[3].trim().isEmpty()) {
                            String[] historyEntries = data[3].split(";");
                            for (String entry : historyEntries) {
                                history.add(entry.trim());
                            }
                        }

                        // Create user object and add to list
                        User user = new User(username, password, watchlist, history);
                        users.add(user);
                    }

                } catch (NumberFormatException e) {
                    // Warn on invalid data
                    System.out.println("Warning: Invalid data format in line: " + line);
                }
            }

            System.out.println("Successfully loaded " + users.size() + " users.");

        } catch (FileNotFoundException e) {
            System.out.println("Error: Users file not found - " + filename);
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        } finally {
            // Close the file reader
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("Error closing file: " + e.getMessage());
                }
            }
        }

        return users;
    }

    // Save all user data back to the CSV file.
    public void saveUsers(String filename, ArrayList<User> users) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(filename));

            // Write header row
            bw.write("Username,Password,Watchlist,History");
            bw.newLine();

            // Write each user's data
            for (User user : users) {
                StringBuilder line = new StringBuilder();
                line.append(user.getUsername()).append(",");
                line.append(user.getPassword()).append(",");
                line.append(listToString(user.getWatchlist())).append(",");
                line.append(listToString(user.getHistory()));

                bw.write(line.toString());
                bw.newLine();
            }

            System.out.println("Successfully saved " + users.size() + " users.");

        } catch (IOException e) {
            System.out.println("Error saving users file: " + e.getMessage());
        } finally {
            // Close writer
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.out.println("Error closing file: " + e.getMessage());
                }
            }
        }
    }

    // Convert a list of strings into a semicolon-separated string.
    private String listToString(ArrayList<String> list) {
        if (list.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

    // Search for a user in the list by username.
    public User findUserByUsername(ArrayList<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
