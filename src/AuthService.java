import java.io.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

public class AuthService {
    private static final String USER_FILE = "users.txt";

    public static boolean register(String username, String email, String password) throws Exception {
        if (userExists(username, email)) return false;

        String salt = generateSalt();
        String hashed = hashPassword(password, salt);

        try (FileWriter fw = new FileWriter(USER_FILE, true)) {
            fw.write(username + ";" + email + ";" + hashed + ";" + salt + "\n");
        }
        return true;
    }

    public static boolean login(String username, String password) throws Exception {
        for (User u : loadUsers()) {
            if (u.getUsername().equals(username)) {
                String hashed = hashPassword(password, u.getSalt());
                if (u.getPasswordHash().equals(hashed))
                    return true;
            }
        }
        return false;
    }

    private static boolean userExists(String username, String email) throws Exception {
        for (User u : loadUsers()) {
            if (u.getUsername().equals(username) || u.getEmail().equals(email))
                return true;
        }
        return false;
    }

    private static List<User> loadUsers() throws Exception {
        List<User> users = new ArrayList<>();
        File f = new File(USER_FILE);
        if (!f.exists()) return users;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) users.add(new User(parts[0], parts[1], parts[2], parts[3]));
            }
        }
        return users;
    }

    private static String hashPassword(String password, String salt) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest((password + salt).getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    private static String generateSalt() {
        byte[] salt = new byte[16]; // 128 битен salt
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
}
