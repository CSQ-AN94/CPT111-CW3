import java.util.List;
import java.util.Scanner;


public class UserRegistration {

    private final Scanner scanner;

    /**
     * 构造函数
     * @param scanner 用于读取用户输入的 Scanner 对象
     */
    public UserRegistration(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * 执行用户注册流程
     * @param existingUsers 已存在的用户列表，用于检查用户名唯一性
     * @return 注册成功则返回新创建的 User 对象，失败则返回 null
     */
    public User registerNewUser(List<User> existingUsers) {
        System.out.println("\n=== New User Registration ===");

        String username;
        while (true) {
            System.out.print("Enter desired username: ");
            username = scanner.nextLine().trim();

            if (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please try again.");
                continue;
            }

            if (isUsernameTaken(username, existingUsers)) {
                System.out.println("Username '" + username + "' is already taken. Please choose another one.");
            } else {
                break; // Username is valid and unique
            }
        }

        String password;
        while (true) {
            System.out.print("Enter password: "); // 明确提示
            password = scanner.nextLine(); // 获取密码输入

            if (password == null || password.trim().isEmpty()) {
                System.out.println("Password cannot be empty. Please enter a password.");
            } else {
                password = password.trim(); // 确保密码是 trim 后的值
                break; // 密码有效则跳出循环
            }
        }  // 循环直到获得非空密码


        // 创建新的 User 对象
        // 注意：User 构造函数中会自动初始化空的 watchedMovies (history) 和 watchlist
        User newUser = new User(username, password); // 使用获取到的有效密码

        System.out.println("Registration successful for user: " + username);
        return newUser; // 返回新创建的用户对象
    }

    /**
     * 检查用户名是否已被占用
     * @param username 要检查的用户名
     * @param existingUsers 已存在的用户列表
     * @return 如果用户名已存在则返回 true，否则返回 false
     */
    private boolean isUsernameTaken(String username, List<User> existingUsers) {
        for (User user : existingUsers) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }


}
