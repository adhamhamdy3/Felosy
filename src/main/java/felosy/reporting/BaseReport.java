package felosy.reporting;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Base class for all reports in the system
 * Simplified for academic purposes
 */
public abstract class BaseReport {
    protected static final Logger LOGGER = Logger.getLogger(BaseReport.class.getName());
    
    protected String reportId;
    protected String reportType;
    protected Date generationDate;
    protected Map<String, Object> reportData;
    
    public BaseReport(String reportId, String reportType) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.generationDate = new Date();
        this.reportData = new HashMap<>();
    }
    
    /**
     * Add data to the report
     */
    public void addData(String key, Object value) {
        reportData.put(key, value);
    }
    
    /**
     * Generate the report content
     */
    public abstract String generateContent();
    
    /**
     * Save the report to a file
     */
    public boolean saveToFile() {
        try {
            String content = generateContent();
            String filename = String.format("%s_%s.txt", 
                reportType.replaceAll("\\s+", "_"),
                reportId);
            
            // Simplified file saving for academic purposes
            System.out.println("Saving report to: " + filename);
            System.out.println("Content:\n" + content);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error saving report", e);
            return false;
        }
    }
    
    // Getters
    public String getReportId() {
        return reportId;
    }
    
    public String getReportType() {
        return reportType;
    }
    
    public Date getGenerationDate() {
        return generationDate;
    }
    
    public Map<String, Object> getReportData() {
        return new HashMap<>(reportData);
    }
    
    @Override
    public String toString() {
        return String.format("%s [id=%s, type=%s, date=%s, dataPoints=%d]",
            getClass().getSimpleName(),
            reportId,
            reportType,
            generationDate,
            reportData.size());
    }
} 