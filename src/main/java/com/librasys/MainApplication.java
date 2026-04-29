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
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/com/librasys/DashboardView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1320, 820);
        scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/dashboard.css").toExternalForm());
        scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/students.css").toExternalForm());
        scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/books.css").toExternalForm());
        scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/shelves.css").toExternalForm());
        scene.getStylesheets().add(MainApplication.class.getResource("/com/librasys/loans.css").toExternalForm());

        stage.setTitle("LibraSys");
        stage.setMinWidth(1080);
        stage.setMinHeight(680);
        stage.setScene(scene);
        stage.show();
    }
}
