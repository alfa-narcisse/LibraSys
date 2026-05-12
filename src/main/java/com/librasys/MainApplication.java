package com.librasys;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Start with the Login view as the first screen
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/com/librasys/LoginView.fxml"));
        Parent root = loader.load(); // On charge l'élément parent dans le fichier fxml (le conteneur et ses contenus)
        Scene scene = new Scene(root, 800, 600); // On lui monte sur scene pour performer
        scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/styleSheet/style.css").toExternalForm());

        stage.setTitle("LibraSys");
        stage.setMinWidth(1080);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();

    }
}
