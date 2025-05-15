module felosy {
    // Original requirements
    requires javafx.controls;
    requires javafx.fxml;
    
    // Add missing module requirements from error log
    requires java.logging;
    requires java.sql;
    
    // Email functionality requirements
    requires java.mail;
    
    // JSON library requirement
    requires org.json;
    requires java.base;
    requires org.apache.pdfbox;

    // Original exports
    opens felosy to javafx.fxml;
    exports felosy;
    
    // Additional exports for your packages that had logging errors
    exports felosy.assetmanagement;
    exports felosy.authentication;
    exports felosy.islamicfinance;
    exports felosy.islamicfinance.config;
    exports felosy.reporting;
    exports felosy.storage;
    exports felosy.utils;
    exports felosy.controllers;
    opens felosy.controllers to javafx.fxml;

    // You might need to open these packages to FXML as well if they contain controllers
    // Uncomment as needed:
    // opens felosy.assetmanagement to javafx.fxml;
    // opens felosy.authentication to javafx.fxml;
    // etc.
}