public class User {
    private String username;
    private String email;
    private String passwordHash;
    private String salt;

    public User(String username, String email, String passwordHash, String salt) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }

    @Override
    public String toString() {
        return username + ";" + email + ";" + passwordHash + ";" + salt;
    }
}
