/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package felosy.reporting;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import felosy.authentication.User;
import felosy.assetmanagement.Portfolio;

/**
 * Report class for generating various types of financial reports
 * Simplified for academic purposes
 */
public class Report extends BaseReport {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance(Locale.US);
    
    private String recipientEmail;
    private boolean isGenerated;
    private User user;
    private Portfolio portfolio;
    private ReportFormat format;
    
    public enum ReportFormat {
        TEXT,
        PDF,
        EXCEL,
        HTML
    }
    
    /**
     * Constructor for creating a new report
     * 
     * @param reportId Unique identifier for the report
     * @param reportType Type of report (e.g., "Zakat", "Compliance", "Portfolio Performance")
     * @param generationDate Date when the report was generated
     * @param user User associated with the report
     */
    public Report(String reportId, String reportType, Date generationDate, User user) {
        super(reportId, reportType);
        this.generationDate = generationDate;
        this.isGenerated = false;
        this.user = user;
        this.format = ReportFormat.TEXT;
        PERCENT_FORMAT.setMaximumFractionDigits(2);
    }
    
    /**
     * Set the portfolio for this report
     */
    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }
    
    /**
     * Set the report format
     */
    public void setFormat(ReportFormat format) {
        this.format = format;
    }
    
    @Override
    public String generateContent() {
        StringBuilder content = new StringBuilder();
        
        // Add header
        content.append(generateHeader());
        
        // Add user information if available
        if (user != null) {
            content.append(generateUserSection());
        }
        
        // Add portfolio information if available
        if (portfolio != null) {
            content.append(generatePortfolioSection());
        }
        
        // Add report data
        content.append(generateDataSection());
        
        // Add footer
        content.append(generateFooter());
        
        isGenerated = true;
        return content.toString();
    }
    
    private String generateHeader() {
        StringBuilder header = new StringBuilder();
        header.append("=".repeat(50)).append("\n");
        header.append(" ".repeat(15)).append(reportType).append("\n");
        header.append("=".repeat(50)).append("\n\n");
        header.append("Report ID: ").append(reportId).append("\n");
        header.append("Generated: ").append(DATE_FORMAT.format(generationDate)).append("\n\n");
        return header.toString();
    }
    
    private String generateUserSection() {
        StringBuilder section = new StringBuilder();
        section.append("=== User Information ===\n");
        section.append("User ID: ").append(user.getUserId()).append("\n");
        section.append("Name: ").append(user.getUserName()).append("\n");
        section.append("Email: ").append(user.getEmail()).append("\n");
        // section.append("User Category: ").append(user.getUserCategory()).append("\n\n");
        return section.toString();
    }
    
    private String generatePortfolioSection() {
        StringBuilder section = new StringBuilder();
        section.append("=== Portfolio Information ===\n");
        section.append("Portfolio ID: ").append(portfolio.getPortfolioId()).append("\n");
        section.append("Name: ").append(portfolio.getName()).append("\n");
        section.append("Description: ").append(portfolio.getDescription()).append("\n");
        section.append("Total Value: ").append(formatCurrency(portfolio.getNetWorth())).append("\n");
        section.append("Number of Assets: ").append(portfolio.getAssets().size()).append("\n");
        section.append("Last Updated: ").append(DATE_FORMAT.format(portfolio.getLastUpdated())).append("\n\n");
        return section.toString();
    }
    
    private String generateDataSection() {
        StringBuilder section = new StringBuilder();
        section.append("=== Report Data ===\n");
        
        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            // Format the value based on its type
            String formattedValue = formatValue(value);
            section.append(key).append(": ").append(formattedValue).append("\n");
        }
        
        section.append("\n");
        return section.toString();
    }
    
    private String generateFooter() {
        StringBuilder footer = new StringBuilder();
        footer.append("=".repeat(50)).append("\n");
        footer.append("End of Report\n");
        footer.append("=".repeat(50)).append("\n");
        return footer.toString();
    }
    
    private String formatValue(Object value) {
        if (value == null) return "N/A";
        
        if (value instanceof BigDecimal) {
            return formatCurrency((BigDecimal) value);
        }
        if (value instanceof Number) {
            if (value instanceof Double || value instanceof Float) {
                return PERCENT_FORMAT.format(((Number) value).doubleValue());
            }
            return CURRENCY_FORMAT.format(((Number) value).doubleValue());
        }
        return value.toString();
    }
    
    private String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
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
     * Export the report in the specified format
     */
    public boolean export() {
        if (!isGenerated) {
            LOGGER.warning("Cannot export: Report has not been generated yet");
            return false;
        }
        
        try {
            switch (format) {
                case PDF:
                    return exportAsPDF();
                case EXCEL:
                    return exportAsExcel();
                case HTML:
                    return exportAsHTML();
                default:
                    return saveToFile();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to export report", e);
            return false;
        }
    }
    
    private boolean exportAsPDF() {
        // TODO: Implement PDF export
        System.out.println("Exporting report as PDF: " + getFileName("pdf"));
        return true;
    }
    
    private boolean exportAsExcel() {
        // TODO: Implement Excel export
        System.out.println("Exporting report as Excel: " + getFileName("xlsx"));
        return true;
    }
    
    private boolean exportAsHTML() {
        // TODO: Implement HTML export
        System.out.println("Exporting report as HTML: " + getFileName("html"));
        return true;
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
    
    public ReportFormat getFormat() {
        return format;
    }
    
    @Override
    public String toString() {
        return String.format("Report [id=%s, type=%s, date=%s, format=%s, dataPoints=%d, generated=%s]",
            reportId,
            reportType,
            DATE_FORMAT.format(generationDate),
            format,
            reportData.size(),
            isGenerated);
    }
}