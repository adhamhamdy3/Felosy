package felosy;

import felosy.services.CryptoDataService;
import felosy.services.GoldDataService;
import felosy.services.RealEstateDataService;
import felosy.services.StockDataService;
import felosy.authentication.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class    App extends Application {

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        Parent root = loadFXML("login");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Felosy");
        stage.setResizable(true);
        stage.centerOnScreen();
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        Parent root = loadFXML(fxml);
        Scene scene = primaryStage.getScene();
        scene.setRoot(root);
        // Optional: auto size based on new root
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        if (fxmlLoader.getLocation() == null) {
            System.err.println("Could not find FXML file: /fxml/" + fxml + ".fxml");
            System.err.println("Class path: " + App.class.getProtectionDomain().getCodeSource().getLocation());
        }
        return fxmlLoader.load();
    }

    @Override
    public void stop() {
        // Save data when the application closes
        GoldDataService.getInstance().shutdown();
        CryptoDataService.getInstance().shutdown();
        RealEstateDataService.getInstance().shutdown();
        StockDataService.getInstance().shutdown();
    }


    public static void main(String[] args) {
        launch();
    }
}
