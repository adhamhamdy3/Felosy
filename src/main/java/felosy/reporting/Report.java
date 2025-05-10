/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.reporting;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.io.File;

/**
 * Report class for generating various types of financial reports
 */
public class Report {
    private String reportId;
    private String reportType;
    private Date generationDate;
    private Map<String, Object> reportData;
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
        this.reportId = reportId;
        this.reportType = reportType;
        this.generationDate = generationDate;
        this.reportData = new HashMap<>();
        this.isGenerated = false;
    }
    
    /**
     * Add data to the report
     * 
     * @param key Data identifier
     * @param value Data value
     */
    public void addData(String key, Object value) {
        reportData.put(key, value);
    }
    
    /**
     * Generate PDF version of the report
     * 
     * @return True if generation was successful
     */
    public boolean generatePDF() {
        System.out.println("Generating PDF report: " + reportId);
        
        if (reportData.isEmpty()) {
            System.err.println("Cannot generate PDF: No data available for report " + reportId);
            return false;
        }
        
        try {
            // Implementation would use a PDF library (like iText or PDFBox) to create the document
            System.out.println("Creating PDF file with " + reportData.size() + " data points");
            System.out.println("PDF report successfully generated: " + getFileName("pdf"));
            isGenerated = true;
            return true;
        } catch (Exception e) {
            System.err.println("Failed to generate PDF report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate Excel version of the report
     * 
     * @return True if generation was successful
     */
    public boolean generateExcel() {
        System.out.println("Generating Excel report: " + reportId);
        
        if (reportData.isEmpty()) {
            System.err.println("Cannot generate Excel: No data available for report " + reportId);
            return false;
        }
        
        try {
            // Implementation would use Excel library like Apache POI
            System.out.println("Creating Excel workbook with " + reportData.size() + " data points");
            System.out.println("Excel report successfully generated: " + getFileName("xlsx"));
            isGenerated = true;
            return true;
        } catch (Exception e) {
            System.err.println("Failed to generate Excel report: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send the report via email
     * 
     * @param recipientEmail Email address to send the report to
     * @return True if email was sent successfully
     */
    public boolean sendEmail(String recipientEmail) {
        if (!isGenerated) {
            System.err.println("Cannot send email: Report has not been generated yet");
            return false;
        }
        
        this.recipientEmail = recipientEmail;
        System.out.println("Sending report " + reportId + " by email to: " + recipientEmail);
        
        try {
            // Implementation would use JavaMail or similar to send the report
            System.out.println("Email sent successfully with report attached");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
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
        File file = new File(getFileName(extension));
        return file.exists();
    }
    
    // Getters and Setters
    public String getReportId() {
        return reportId;
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }
    
    public Date getGenerationDate() {
        return generationDate;
    }
    
    public Map<String, Object> getReportData() {
        return new HashMap<>(reportData);
    }
    
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