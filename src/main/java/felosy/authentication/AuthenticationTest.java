package felosy.authentication;

/**
 * Test class to demonstrate the functionality of the Authentication service
 */
public class AuthenticationTest {
    
    public static void main(String[] args) {
        System.out.println("Initializing Authentication System...");
        Authentication.initialize();
        
        // Test user registration
        testUserRegistration();
        
        // Test user login
        String sessionToken = testUserLogin();
        
        // Test get current user
        testGetCurrentUser(sessionToken);
        
        // Test password change
        testPasswordChange();
        
        // Test profile update
        testProfileUpdate();
        
        // Test logout
        testLogout(sessionToken);
        
        System.out.println("\nAll tests completed.");
    }
    
    private static void testUserRegistration() {
        System.out.println("\n=== Testing User Registration ===");
        
        // Test valid registration
        User user = Authentication.registerUser("testuser", "test@example.com", "Password123!");
        if (user != null) {
            System.out.println("User registered successfully: " + user);
        } else {
            System.out.println("User registration failed");
        }
        
        // Test duplicate email
        User duplicateUser = Authentication.registerUser("anotheruser", "test@example.com", "AnotherPass123!");
        if (duplicateUser == null) {
            System.out.println("Duplicate email check passed");
        } else {
            System.out.println("Duplicate email check failed");
        }
        
        // Test invalid username
        User invalidUser = Authentication.registerUser("a", "invalid@example.com", "Password123!");
        if (invalidUser == null) {
            System.out.println("Invalid username check passed");
        } else {
            System.out.println("Invalid username check failed");
        }
        
        // Test invalid password
        User weakPasswordUser = Authentication.registerUser("weakuser", "weak@example.com", "weak");
        if (weakPasswordUser == null) {
            System.out.println("Weak password check passed");
        } else {
            System.out.println("Weak password check failed");
        }
    }
    
    private static String testUserLogin() {
        System.out.println("\n=== Testing User Login ===");
        
        // Test valid login with username
        String sessionToken = Authentication.login("testuser", "Password123!");
        if (sessionToken != null) {
            System.out.println("Login with username successful, session token: " + sessionToken.substring(0, 10) + "...");
        } else {
            System.out.println("Login with username failed");
        }
        
        // Test valid login with email
        String emailSessionToken = Authentication.login("test@example.com", "Password123!");
        if (emailSessionToken != null) {
            System.out.println("Login with email successful");
        } else {
            System.out.println("Login with email failed");
        }
        
        // Test invalid credentials
        String invalidSession = Authentication.login("testuser", "WrongPassword123!");
        if (invalidSession == null) {
            System.out.println("Invalid password check passed");
        } else {
            System.out.println("Invalid password check failed");
        }
        
        // Test non-existent user
        String nonExistentSession = Authentication.login("nonexistentuser", "Password123!");
        if (nonExistentSession == null) {
            System.out.println("Non-existent user check passed");
        } else {
            System.out.println("Non-existent user check failed");
        }
        
        return sessionToken;
    }
    
    private static void testGetCurrentUser(String sessionToken) {
        System.out.println("\n=== Testing Get Current User ===");
        
        // Test valid session
        User currentUser = Authentication.getCurrentUser(sessionToken);
        if (currentUser != null) {
            System.out.println("Current user retrieved successfully: " + currentUser);
        } else {
            System.out.println("Failed to retrieve current user");
        }
        
        // Test invalid session
        User invalidUser = Authentication.getCurrentUser("invalid-session-token");
        if (invalidUser == null) {
            System.out.println("Invalid session token check passed");
        } else {
            System.out.println("Invalid session token check failed");
        }
    }
    
    private static void testPasswordChange() {
        System.out.println("\n=== Testing Password Change ===");
        
        // Get user ID
        User user = Authentication.findUserByEmail("test@example.com");
        if (user == null) {
            System.out.println("User not found for password change test");
            return;
        }
        
        // Test valid password change
        boolean changed = Authentication.changePassword(user.getUserId(), "Password123!", "NewPassword123!");
        if (changed) {
            System.out.println("Password changed successfully");
            
            // Verify new password works for login
            String newSession = Authentication.login("testuser", "NewPassword123!");
            if (newSession != null) {
                System.out.println("Login with new password successful");
            } else {
                System.out.println("Login with new password failed");
            }
            
            // Change back to original password for other tests
            Authentication.changePassword(user.getUserId(), "NewPassword123!", "Password123!");
        } else {
            System.out.println("Password change failed");
        }
        
        // Test invalid current password
        boolean invalidChange = Authentication.changePassword(user.getUserId(), "WrongPassword", "AnotherPassword123!");
        if (!invalidChange) {
            System.out.println("Invalid current password check passed");
        } else {
            System.out.println("Invalid current password check failed");
        }
    }
    
    private static void testProfileUpdate() {
        System.out.println("\n=== Testing Profile Update ===");
        
        // Get user ID
        User user = Authentication.findUserByEmail("test@example.com");
        if (user == null) {
            System.out.println("User not found for profile update test");
            return;
        }
        
        // Test username update
        boolean usernameUpdated = Authentication.updateUserProfile(user.getUserId(), "updateduser", null);
        if (usernameUpdated) {
            System.out.println("Username updated successfully");
            
            // Verify update
            User updatedUser = Authentication.findUserByUsernameOrEmail("updateduser");
            if (updatedUser != null) {
                System.out.println("Updated user retrieved: " + updatedUser);
            } else {
                System.out.println("Failed to retrieve updated user");
            }
        } else {
            System.out.println("Username update failed");
        }
        
        // Test email update
        boolean emailUpdated = Authentication.updateUserProfile(user.getUserId(), null, "updated@example.com");
        if (emailUpdated) {
            System.out.println("Email updated successfully");
            
            // Verify update
            User updatedUser = Authentication.findUserByEmail("updated@example.com");
            if (updatedUser != null) {
                System.out.println("User with updated email retrieved: " + updatedUser);
            } else {
                System.out.println("Failed to retrieve user with updated email");
            }
        } else {
            System.out.println("Email update failed");
        }
    }
    
    private static void testLogout(String sessionToken) {
        System.out.println("\n=== Testing Logout ===");
        
        // Test valid logout
        boolean loggedOut = Authentication.logout(sessionToken);
        if (loggedOut) {
            System.out.println("Logout successful");
            
            // Verify session is invalidated
            User user = Authentication.getCurrentUser(sessionToken);
            if (user == null) {
                System.out.println("Session invalidation check passed");
            } else {
                System.out.println("Session invalidation check failed");
            }
        } else {
            System.out.println("Logout failed");
        }
        
        // Test invalid session token
        boolean invalidLogout = Authentication.logout("invalid-session-token");
        if (!invalidLogout) {
            System.out.println("Invalid session token logout check passed");
        } else {
            System.out.println("Invalid session token logout check failed");
        }
    }
}