package com.librasys.ui;

import com.librasys.controller.StudentsController.Student;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.function.Consumer;

public class StudentCard extends VBox {
    private final Student student;

    public StudentCard(Student student, Consumer<Student> onSelect) {
        this.student = student;
        getStyleClass().add("student-card");
        setSpacing(12);
        setAlignment(Pos.TOP_CENTER);
        setPrefSize(190, 240);
        setMinSize(190, 240);
        setMaxSize(190, 240);

        Circle avatar = new Circle(34);
        avatar.getStyleClass().add("student-avatar");
        StackPane avatarWrap = new StackPane(avatar);
        avatarWrap.getStyleClass().add("student-avatar-wrap");

        Label nameLabel = new Label(student.getFullName());
        nameLabel.getStyleClass().add("student-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(162);
        nameLabel.setAlignment(Pos.CENTER);

        Label matriculeLabel = new Label(student.getMatricule());
        matriculeLabel.getStyleClass().add("student-matricule");

        Label loanChip = new Label(student.getActiveLoans() + " prêts");
        loanChip.getStyleClass().add("loan-chip");
        loanChip.getStyleClass().add("loan-chip-active");
        if (student.isNearDelay()) {
            loanChip.getStyleClass().add("loan-chip-warning");
        }

        getChildren().addAll(avatarWrap, nameLabel, matriculeLabel, loanChip);

        setOnMouseClicked(event -> onSelect.accept(this.student));
    }
}
