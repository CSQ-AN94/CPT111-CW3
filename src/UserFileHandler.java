import java.io.*;
import java.util.ArrayList;

/**
 * UserFileHandler类 - 处理用户数据的文件读写
 */
public class UserFileHandler {

    /**
     * 从CSV文件加载用户数据
     * @param filename 文件路径
     * @return 用户列表
     */
    public ArrayList<User> loadUsers(String filename) {
        ArrayList<User> users = new ArrayList<>();
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
                    String[] data = line.split(",");

                    if (data.length >= 2) {
                        String username = data[0].trim();
                        String password = data[1].trim();

                        // 解析Watchlist（格式：M008;M015）
                        ArrayList<String> watchlist = new ArrayList<>();
                        if (data.length > 2 && !data[2].trim().isEmpty()) {
                            String[] watchIds = data[2].split(";");
                            for (String id : watchIds) {
                                watchlist.add(id.trim());
                            }
                        }

                        // 解析History（格式：M001@2025-07-12;M011@2025-08-10）
                        ArrayList<String> history = new ArrayList<>();
                        if (data.length > 3 && !data[3].trim().isEmpty()) {
                            String[] historyEntries = data[3].split(";");
                            for (String entry : historyEntries) {
                                history.add(entry.trim());
                            }
                        }

                        User user = new User(username, password, watchlist, history);
                        users.add(user);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid data format in line: " + line);
                }
            }

            System.out.println("Successfully loaded " + users.size() + " users.");

        } catch (FileNotFoundException e) {
            System.err.println("Error: Users file not found - " + filename);
        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e.getMessage());
                }
            }
        }

        return users;
    }

    /**
     * 保存用户数据到CSV文件
     * @param filename 文件路径
     * @param users 用户列表
     */
    public void saveUsers(String filename, ArrayList<User> users) {
        BufferedWriter bw = null;

        try {
            bw = new BufferedWriter(new FileWriter(filename));

            // 写入标题行
            bw.write("Username,Password,Watchlist,History");
            bw.newLine();

            // 写入每个用户的数据
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
            System.err.println("Error saving users file: " + e.getMessage());
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * 将整数列表转换为字符串（用分号分隔）
     * @param list 字符串列表
     * @return 分号分隔的字符串
     */
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

    /**
     * 根据用户名查找用户
     * @param users 用户列表
     * @param username 用户名
     * @return 找到的用户，如果没找到返回null
     */
    public User findUserByUsername(ArrayList<User> users, String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
