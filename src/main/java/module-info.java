module com.example.librasys {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens com.example.librasys to javafx.fxml;
    opens com.example.librasys.controller to javafx.fxml;
    opens com.librasys.controller to javafx.fxml;
    exports com.example.librasys;
    exports com.example.librasys.model;
    exports com.example.librasys.service;
    exports com.librasys.controller;
}