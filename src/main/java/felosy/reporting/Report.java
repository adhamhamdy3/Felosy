/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.reporting;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Report class for generating various types of financial reports
 * Simplified for academic purposes
 */
public class Report extends BaseReport {
    private String recipientEmail;
    private boolean isGenerated;
    
    /**
     * Constructor for creating a new report
     * 
     * @param reportId Unique identifier for the report
     * @param reportType Type of report (e.g., "Zakat", "Compliance", "Portfolio Performance")
     * @param generationDate Date when the report was generated
     */
    public Report(String reportId, String reportType, Date generationDate) {
        super(reportId, reportType);
        this.generationDate = generationDate;
        this.isGenerated = false;
    }
    
    @Override
    public String generateContent() {
        StringBuilder content = new StringBuilder();
        content.append("=== ").append(reportType).append(" ===\n");
        content.append("Report ID: ").append(reportId).append("\n");
        content.append("Generated: ").append(generationDate).append("\n\n");
        
        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            content.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        isGenerated = true;
        return content.toString();
    }
    
    /**
     * Send the report via email
     */
    public boolean sendEmail(String recipientEmail) {
        if (!isGenerated) {
            LOGGER.warning("Cannot send email: Report has not been generated yet");
            return false;
        }
        
        this.recipientEmail = recipientEmail;
        LOGGER.info("Sending report " + reportId + " by email to: " + recipientEmail);
        
        try {
            // Simplified email sending for academic purposes
            System.out.println("Email content:\n" + generateContent());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send email", e);
            return false;
        }
    }
    
    /**
     * Send the report via email to the previously specified recipient
     * 
     * @return True if email was sent successfully
     */
    public boolean sendEmail() {
        if (this.recipientEmail == null || this.recipientEmail.isEmpty()) {
            System.err.println("Cannot send email: No recipient specified");
            return false;
        }
        return sendEmail(this.recipientEmail);
    }
    
    /**
     * Download the report
     * 
     * @return True if download was initiated successfully
     */
    public boolean download() {
        if (!isGenerated) {
            System.err.println("Cannot download: Report has not been generated yet");
            return false;
        }
        
        System.out.println("Downloading report: " + reportId);
        
        try {
            // Implementation would handle file download logic
            System.out.println("Report downloaded successfully to user's downloads folder");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to download report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate a standardized filename for the report
     * 
     * @param extension File extension without dot
     * @return Formatted filename
     */
    private String getFileName(String extension) {
        String dateStr = String.format("%1$tY%1$tm%1$td", generationDate);
        return reportType.replaceAll("\\s+", "_") + "_" + dateStr + "_" + reportId + "." + extension;
    }
    
    /**
     * Check if a report file exists
     * 
     * @param extension File extension to check
     * @return True if the file exists
     */
    public boolean fileExists(String extension) {
        java.io.File file = new java.io.File(getFileName(extension));
        return file.exists();
    }
    
    // Getters and Setters
    public boolean isGenerated() {
        return isGenerated;
    }
    
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;    
    }
    
    public String getRecipientEmail() {
        return recipientEmail;
    }
    
    @Override
    public String toString() {
        return "Report [reportId=" + reportId + 
               ", reportType=" + reportType + 
               ", generationDate=" + generationDate + 
               ", dataPoints=" + reportData.size() + 
               ", isGenerated=" + isGenerated + "]";
    }
}