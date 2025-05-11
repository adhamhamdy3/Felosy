package felosy.authentication;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Authentication {
    private static final Logger LOGGER = Logger.getLogger(Authentication.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;
    private static final int DEFAULT_SESSION_TIMEOUT = 30; // minutes
    
    // Store active sessions
    private static final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();
    
    private String token;
    private boolean isLoggedIn;
    private int sessionTimeout;
    private LocalDateTime lastActivityTime;
    private User currentUser;
    private EmailService emailService;
    
    private static class SessionData {
        private final User user;
        private LocalDateTime expiryTime;
        private LocalDateTime lastActivityTime;
        
        public SessionData(User user, LocalDateTime expiryTime) {
            this.user = user;
            this.expiryTime = expiryTime;
            this.lastActivityTime = LocalDateTime.now();
        }
    }
    
    public Authentication() {
        this.isLoggedIn = false;
        this.token = null;
        this.sessionTimeout = DEFAULT_SESSION_TIMEOUT;
        this.lastActivityTime = LocalDateTime.now();
    }
    
    /**
     * Attempts to log in a user with email verification
     * @param user The user to log in
     * @return true if login is successful
     */
    public boolean login(User user) {
        if (user == null || user.getEmail() == null) {
            LOGGER.warning("Login attempt with null user or email");
            return false;
        }
        
        try {
            // Initialize email service for the user
            emailService = new EmailService(user.getEmail());
            
            // Generate and send OTP
            if (!emailService.generateAndSendOTP()) {
                LOGGER.warning("Failed to send OTP to: " + user.getEmail());
                return false;
            }
            
            // Store user temporarily
            this.currentUser = user;
            LOGGER.info("OTP sent successfully to: " + user.getEmail());
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during login process", e);
            return false;
        }
    }
    
    /**
     * Verifies OTP and completes the login process
     * @param otp The OTP to verify
     * @return true if OTP is valid and login is successful
     */
    public boolean verifyOTPAndCompleteLogin(String otp) {
        if (currentUser == null || emailService == null) {
            LOGGER.warning("No pending login found");
            return false;
        }
        
        if (emailService.verifyOTP(otp)) {
            this.token = generateSecureToken();
            this.isLoggedIn = true;
            this.lastActivityTime = LocalDateTime.now();
            
            // Store session data
            LocalDateTime sessionExpiry = LocalDateTime.now().plusMinutes(sessionTimeout);
            activeSessions.put(token, new SessionData(currentUser, sessionExpiry));
            
            LOGGER.info("User " + currentUser.getUserName() + " logged in successfully");
            return true;
        }
        
        LOGGER.warning("Invalid OTP provided for user: " + currentUser.getUserName());
        return false;
    }
    
    /**
     * Logs out the current user
     */
    public void logout() {
        if (token != null) {
            activeSessions.remove(token);
        }
        this.token = null;
        this.isLoggedIn = false;
        this.currentUser = null;
        this.emailService = null;
        LOGGER.info("User logged out successfully");
    }
    
    /**
     * Verifies if the current session is valid
     * @return true if the session is valid
     */
    public boolean verifySession() {
        if (!isLoggedIn || token == null) {
            return false;
        }
        
        SessionData sessionData = activeSessions.get(token);
        if (sessionData == null) {
            LOGGER.warning("No active session found for token");
            return false;
        }
        
        // Check if session has expired
        if (LocalDateTime.now().isAfter(sessionData.expiryTime)) {
            LOGGER.warning("Session expired for user: " + sessionData.user.getUserName());
            logout();
            return false;
        }
        
        // Update last activity time
        sessionData.lastActivityTime = LocalDateTime.now();
        this.lastActivityTime = sessionData.lastActivityTime;
        
        return true;
    }
    
    /**
     * Extends the current session timeout
     * @param additionalMinutes Minutes to add to the session timeout
     * @return true if session was extended successfully
     */
    public boolean extendSession(int additionalMinutes) {
        if (!verifySession()) {
            return false;
        }
        
        SessionData sessionData = activeSessions.get(token);
        if (sessionData != null) {
            sessionData.expiryTime = sessionData.expiryTime.plusMinutes(additionalMinutes);
            LOGGER.info("Session extended for user: " + sessionData.user.getUserName());
            return true;
        }
        
        return false;
    }
    
    /**
     * Generates a secure random token
     * @return The generated token
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    // Getters and setters
    public String getToken() {
        return token;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public int getSessionTimeout() {
        return sessionTimeout;
    }
    
    public void setSessionTimeout(int sessionTimeout) {
        if (sessionTimeout > 0) {
            this.sessionTimeout = sessionTimeout;
            // Update existing session if logged in
            if (isLoggedIn && token != null) {
                SessionData sessionData = activeSessions.get(token);
                if (sessionData != null) {
                    sessionData.expiryTime = LocalDateTime.now().plusMinutes(sessionTimeout);
                }
            }
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }
}