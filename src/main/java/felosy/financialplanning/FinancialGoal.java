/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.financialplanning;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FinancialGoal {
    private static final Logger LOGGER = Logger.getLogger(FinancialGoal.class.getName());
    
    private final String goalId;
    private final String goalType;
    private final double targetAmount;
    private double currentAmount;
    private LocalDateTime targetDate;
    private final LocalDateTime creationDate;
    private LocalDateTime lastUpdateDate;
    private GoalPriority priority;
    private GoalStatus status;
    
    public enum GoalPriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
    
    public enum GoalStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        ON_HOLD
    }
    
    public FinancialGoal(String goalType, double targetAmount) {
        this.goalId = UUID.randomUUID().toString();
        this.goalType = goalType;
        this.targetAmount = targetAmount;
        this.currentAmount = 0.0;
        this.creationDate = LocalDateTime.now();
        this.lastUpdateDate = creationDate;
        this.priority = GoalPriority.MEDIUM;
        this.status = GoalStatus.NOT_STARTED;
    }
    
    /**
     * Updates the progress towards the goal
     * @param amount Amount to add to current progress
     * @return true if update was successful
     */
    public boolean updateProgress(double amount) {
        try {
            if (amount < 0) {
                LOGGER.warning("Cannot update progress with negative amount");
                return false;
            }
            
            this.currentAmount += amount;
            this.lastUpdateDate = LocalDateTime.now();
            
            // Update status based on progress
            if (currentAmount >= targetAmount) {
                this.status = GoalStatus.COMPLETED;
            } else if (this.status == GoalStatus.NOT_STARTED) {
                this.status = GoalStatus.IN_PROGRESS;
            }
            
            LOGGER.info("Updated progress for goal: " + goalType + ". New amount: " + currentAmount);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update progress", e);
            return false;
        }
    }
    
    /**
     * Calculates the progress percentage towards the goal
     * @return Progress percentage (0-100)
     */
    public double getProgressPercentage() {
        return (currentAmount / targetAmount) * 100;
    }
    
    /**
     * Calculates the remaining amount needed to reach the goal
     * @return Remaining amount
     */
    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }
    
    /**
     * Updates the target date for the goal
     * @param targetDate New target date
     * @return true if update was successful
     */
    public boolean updateTargetDate(LocalDateTime targetDate) {
        try {
            if (targetDate.isBefore(creationDate)) {
                LOGGER.warning("Target date cannot be before creation date");
                return false;
            }
            
            this.targetDate = targetDate;
            this.lastUpdateDate = LocalDateTime.now();
            LOGGER.info("Updated target date for goal: " + goalType);
            return true;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update target date", e);
            return false;
        }
    }
    
    /**
     * Updates the priority of the goal
     * @param priority New priority level
     * @return true if update was successful
     */
    public boolean updatePriority(GoalPriority priority) {
        try {
            this.priority = priority;
            this.lastUpdateDate = LocalDateTime.now();
            LOGGER.info("Updated priority for goal: " + goalType + " to " + priority);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update priority", e);
            return false;
        }
    }
    
    /**
     * Updates the status of the goal
     * @param status New status
     * @return true if update was successful
     */
    public boolean updateStatus(GoalStatus status) {
        try {
            this.status = status;
            this.lastUpdateDate = LocalDateTime.now();
            LOGGER.info("Updated status for goal: " + goalType + " to " + status);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to update status", e);
            return false;
        }
    }
    
    // Getters
    public String getGoalId() {
        return goalId;
    }
    
    public String getGoalType() {
        return goalType;
    }
    
    public double getTargetAmount() {
        return targetAmount;
    }
    
    public double getCurrentAmount() {
        return currentAmount;
    }
    
    public LocalDateTime getTargetDate() {
        return targetDate;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public LocalDateTime getLastUpdateDate() {
        return lastUpdateDate;
    }
    
    public GoalPriority getPriority() {
        return priority;
    }
    
    public GoalStatus getStatus() {
        return status;
    }
}