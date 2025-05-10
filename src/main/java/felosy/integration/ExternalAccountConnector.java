package felosy.integration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Base connector class for external financial services
 */
public class ExternalAccountConnector {
    private String connectorId;
    private String apiKey;
    private boolean connectionStatus;
    private String baseUrl;
    private Map<String, String> defaultHeaders;
    private static final Logger LOGGER = Logger.getLogger(ExternalAccountConnector.class.getName());

    public ExternalAccountConnector(String connectorId, String apiKey, String baseUrl) {
        this.connectorId = connectorId;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.connectionStatus = false;
        this.defaultHeaders = new HashMap<>();
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("Authorization", "Bearer " + apiKey);
    }

    /**
     * Establishes a connection with the external service
     * @return true if connection was successful
     */
    public boolean establishConnection() {
        try {
            // Perform a test call to verify connectivity
            Response response = sendRequest("/test", "GET", null);
            connectionStatus = response.isSuccessful();
            
            if (connectionStatus) {
                LOGGER.info("Successfully established connection to external service: " + connectorId);
            } else {
                LOGGER.warning("Failed to establish connection to external service: " + connectorId);
            }
            
            return connectionStatus;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error establishing connection", e);
            connectionStatus = false;
            return false;
        }
    }

    /**
     * Sends a request to the specified endpoint
     * @param endpoint API endpoint
     * @param method HTTP method (GET, POST, etc.)
     * @param payload Request payload (can be null for GET requests)
     * @return Response object
     */
    public Response sendRequest(String endpoint, String method, JSONObject payload) {
        if (!connectionStatus && !endpoint.equals("/test")) {
            LOGGER.warning("Attempting to send request without established connection");
            throw new IllegalStateException("Connection not established. Call establishConnection() first.");
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            
            // Add default headers
            for (Map.Entry<String, String> header : defaultHeaders.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }
            
            // Add payload for POST/PUT requests
            if (payload != null && (method.equals("POST") || method.equals("PUT"))) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            
            // Get response
            int statusCode = connection.getResponseCode();
            StringBuilder responseBody = new StringBuilder();
            
            try (Scanner scanner = new Scanner(
                    statusCode >= 400 
                    ? connection.getErrorStream() 
                    : connection.getInputStream())) {
                while (scanner.hasNextLine()) {
                    responseBody.append(scanner.nextLine());
                }
            }
            
            Response response = new Response(statusCode, responseBody.toString());
            
            // Add response headers
            for (String headerName : connection.getHeaderFields().keySet()) {
                if (headerName != null) {
                    response.addHeader(headerName, connection.getHeaderField(headerName));
                }
            }
            
            return response;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error sending request to " + endpoint, e);
            return new Response(500, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Process API response data
     * @param data Response data to process
     * @return Processed object
     */
    public Object handleResponse(Object data) {
        // Default implementation - override in subclasses for specific processing
        if (data instanceof Response) {
            Response response = (Response) data;
            if (!response.isSuccessful()) {
                LOGGER.warning("Received unsuccessful response: " + response);
                // Handle different error codes
                if (response.getStatusCode() == 401) {
                    connectionStatus = false;
                    LOGGER.severe("Authentication failed. Please check API credentials.");
                }
            }
            return response.getBody();
        }
        return data;
    }

    /**
     * Close the connection to the external service
     * @return true if disconnection was successful
     */
    public boolean closeConnection() {
        try {
            // Implement logout/disconnect logic if the API provides one
            if (connectionStatus) {
                Response response = sendRequest("/logout", "POST", null);
                connectionStatus = false;
                LOGGER.info("Connection closed for: " + connectorId);
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during connection closure", e);
            connectionStatus = false;
            return false;
        }
    }

    // Getters and Setters
    
    public String getConnectorId() {
        return connectorId;
    }

    public boolean isConnected() {
        return connectionStatus;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        defaultHeaders.put("Authorization", "Bearer " + apiKey);
    }
}