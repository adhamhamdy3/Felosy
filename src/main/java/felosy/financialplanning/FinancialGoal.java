/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.financialplanning;

import java.util.UUID;

public class FinancialGoal {
    private String goalId;
    private String goalType;
    private float targetAmount;

    public FinancialGoal(String goalType, float targetAmount) {
        this.goalId = UUID.randomUUID().toString();
        this.goalType = goalType;
        this.targetAmount = targetAmount;
    }

    public boolean addGoal() {
        // Implementation for adding a new financial goal
        System.out.println("Adding new " + goalType + " goal with target: " + targetAmount);
        return true;
    }

    public float trackProgress() {
        // Implementation for tracking goal progress
        // This is a placeholder - real implementation would track actual savings/investments
        System.out.println("Tracking progress for goal: " + goalType);
        return 65.5f; // Example progress percentage
    }

    public boolean updateGoal() {
        // Implementation for updating goal details
        System.out.println("Updating goal: " + goalType);
        return true;
    }

    // Getters and setters
    public String getGoalId() {
        return goalId;
    }

    public String getGoalType() {
        return goalType;
    }

    public void setGoalType(String goalType) {
        this.goalType = goalType;
    }

    public float getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(float targetAmount) {
        this.targetAmount = targetAmount;
    }
}