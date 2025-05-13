package felosy.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Utility class for displaying various types of alerts in the application.
 */
public class AlertUtil {

    /**
     * Shows an alert dialog with the specified parameters.
     *
     * @param alertType The type of alert to display
     * @param title The title of the alert
     * @param header The header text of the alert
     * @param content The content text of the alert
     * @return The ButtonType that was clicked
     */
    public static ButtonType showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL);
    }

    /**
     * Shows an error alert with the specified message.
     *
     * @param message The error message to display
     */
    public static void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Error", "An error occurred", message);
    }

    /**
     * Shows a confirmation dialog with the specified message.
     *
     * @param message The confirmation message to display
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String message) {
        ButtonType result = showAlert(
                Alert.AlertType.CONFIRMATION,
                "Confirmation",
                "Please confirm",
                message
        );
        return result == ButtonType.OK;
    }

    /**
     * Shows an information alert with the specified message.
     *
     * @param message The information message to display
     */
    public static void showInformation(String message) {
        showAlert(Alert.AlertType.INFORMATION, "Information", null, message);
    }

    /**
     * Shows a warning alert with the specified message.
     *
     * @param message The warning message to display
     */
    public static void showWarning(String message) {
        showAlert(Alert.AlertType.WARNING, "Warning", "Warning", message);
    }
}