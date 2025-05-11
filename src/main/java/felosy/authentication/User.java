package felosy.authentication;
import java.io.Serializable;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Basic user information
    private String userId;
    private String userName;
    private String email;
    private String passwordHash; // Store hashed password
    
    // Demographic fields
    private int age;
    private Gender gender;
    private String postalCode;
    private EducationLevel educationLevel;
    private double income;
    private double techUsagePattern; // Scale from 0.0 -> 1.0
    private UserCategory userCategory;
    
    // Enums for demographic data
    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum EducationLevel {
        HIGH_SCHOOL, SOME_COLLEGE, ASSOCIATES_DEGREE, 
        BACHELORS_DEGREE, MASTERS_DEGREE, DOCTORAL_DEGREE, 
        PROFESSIONAL_DEGREE, OTHER
    }
    
    public enum UserCategory {
        YOUNG_ADULT, MID_CAREER_PROFESSIONAL, HIGH_NET_WORTH_INDIVIDUAL,
        RETIREE, SELF_EMPLOYED_ENTREPRENEUR
    }
    
    // Basic constructor with essential fields
    public User(String userName, String email, String password) {
        this.userId = UUID.randomUUID().toString(); // to be checked with checkers in utilities, to ensure that it does not exist before
        this.userName = userName; // to be checked with checkers in utilities
        this.email = email; // to be checked with checkers in utilities
        this.passwordHash = hashPassword(password);
    }
    
    // Full constructor with all fields
    public User(String userName, String email, String password, 
                int age, Gender gender, String postalCode, 
                EducationLevel educationLevel, double income, double techUsagePattern) {
        this(userName, email, password);
        this.age = age;
        this.gender = gender;
        this.postalCode = postalCode;
        this.educationLevel = educationLevel;
        this.income = income;
        setTechUsagePattern(techUsagePattern); // Using setter for validation
        determineUserCategory(); // Determine category based on age and other factors
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
    
    public DemographicDetails getDemographicDetails() {
        // Return demographic details
        return new DemographicDetails(age, gender, postalCode, 
                educationLevel, income, techUsagePattern, userCategory);
    }
    
    // Method to determine user category based on age and other factors
    private void determineUserCategory() {
        if (age >= 65) {
            this.userCategory = UserCategory.RETIREE;
        } else if (age >= 30 && age <= 50) {
            if (income > 200000) {
                this.userCategory = UserCategory.HIGH_NET_WORTH_INDIVIDUAL;
            } else {
                this.userCategory = UserCategory.MID_CAREER_PROFESSIONAL;
            }
        } else if (age < 30) {
            this.userCategory = UserCategory.YOUNG_ADULT;
        }
        
        // Override based on income if very high
        if (income > 500000) {
            this.userCategory = UserCategory.HIGH_NET_WORTH_INDIVIDUAL;
        }
    }
    
    // Manual override for user category
    public void setUserCategory(UserCategory category) {
        this.userCategory = category;
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
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
        determineUserCategory(); // Recalculate category when age changes
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public EducationLevel getEducationLevel() {
        return educationLevel;
    }
    
    public void setEducationLevel(EducationLevel educationLevel) {
        this.educationLevel = educationLevel;
    }
    
    public double getIncome() {
        return income;
    }
    
    public void setIncome(double income) {
        this.income = income;
        determineUserCategory(); // Recalculate category when income changes
    }
    
    public double getTechUsagePattern() {
        return techUsagePattern;
    }
    
    public void setTechUsagePattern(double techUsagePattern) {
        // Ensure value is between 0.0 and 1.0
        if (techUsagePattern < 0.0 || techUsagePattern > 1.0) {
            throw new IllegalArgumentException("Tech usage pattern must be between 0.0 and 1.0");
        }
        this.techUsagePattern = techUsagePattern;
    }
    
    public UserCategory getUserCategory() {
        return userCategory;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", gender=" + gender +
                ", postalCode='" + postalCode + '\'' +
                ", educationLevel=" + educationLevel +
                ", income=" + String.format("$%,.2f", income) +
                ", techUsagePattern=" + String.format("%.2f", techUsagePattern) +
                ", userCategory=" + userCategory +
                '}';
    }
}

// Class to return demographic details
class DemographicDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int age;
    private User.Gender gender;
    private String postalCode;
    private User.EducationLevel educationLevel;
    private double income;
    private double techUsagePattern;
    private User.UserCategory userCategory;
    
    public DemographicDetails(int age, User.Gender gender, String postalCode, 
                             User.EducationLevel educationLevel,
                             double income, double techUsagePattern, 
                             User.UserCategory userCategory) {
        this.age = age;
        this.gender = gender;
        this.postalCode = postalCode;
        this.educationLevel = educationLevel;
        this.income = income;
        this.techUsagePattern = techUsagePattern;
        this.userCategory = userCategory;
    }
    
    // Getters for all fields
    public int getAge() { return age; }
    public User.Gender getGender() { return gender; }
    public String getPostalCode() { return postalCode; }
    public User.EducationLevel getEducationLevel() { return educationLevel; }
    public double getIncome() { return income; }
    public double getTechUsagePattern() { return techUsagePattern; }
    public User.UserCategory getUserCategory() { return userCategory; }
    
    @Override
    public String toString() {
        return "DemographicDetails{" +
                "age=" + age +
                ", gender=" + gender +
                ", postalCode='" + postalCode + '\'' +
                ", educationLevel=" + educationLevel +
                ", income=" + String.format("$%,.2f", income) +
                ", techUsagePattern=" + String.format("%.2f", techUsagePattern) +
                ", userCategory=" + userCategory +
                '}';
    }
}