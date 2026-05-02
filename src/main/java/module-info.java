module com.example.librasys {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jetbrains.annotations;
    requires java.sql;
    exports com.librasys;
    opens com.librasys.controller to javafx.fxml;
    exports com.librasys.controller;
    exports com.librasys.ui;
}