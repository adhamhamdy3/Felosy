/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.authentication;

import java.util.*;

public class User {
    private String userId;
    private String userName;
    private String email;

    public User(String userName, String email) {
        this.userId = UUID.randomUUID().toString();
        this.userName = userName;
        this.email = email;
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
}
