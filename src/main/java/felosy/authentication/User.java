/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.authentication;

import java.io.Serializable;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String userId;
    private String userName;
    private String email;
    private String passwordHash; // Store hashed password instead of plain text

    public User(String userName, String email, String password) {
        this.userId = UUID.randomUUID().toString();
        this.userName = userName;
        this.email = email;
        this.passwordHash = hashPassword(password);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean authenticate(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    public boolean register() {
        // Implementation for user registration
        System.out.println("Registering user: " + userName);
        return true;
    }

    public boolean updateProfile() {
        // Implementation for updating user profile
        System.out.println("Updating profile for user: " + userName);
        return true;
    }

    public UserDetails getDetails() {
        // Return user details
        return new UserDetails(userId, userName, email);
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String newPassword) {
        this.passwordHash = hashPassword(newPassword);
    }
}
