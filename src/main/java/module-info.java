module com.example.librasys {
    requires javafx.controls;
    requires javafx.fxml;
    exports com.librasys;
    opens com.librasys.controller to javafx.fxml;
    exports com.librasys.controller;
    exports com.librasys.ui;
}