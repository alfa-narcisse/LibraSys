package com.example.librasys;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private static final String APP_TITLE = "LibraSys - Connexion";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/com/librasys/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 680);
        scene.getStylesheets().add(HelloApplication.class.getResource("/com/librasys/style.css").toExternalForm());
        stage.setTitle(APP_TITLE);
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }
}
