package com.itss.importorder;

import com.itss.importorder.boundary.CuaSoChinhBoundary;
import com.itss.importorder.boundary.DangNhapBoundary;
import com.itss.importorder.entity.NguoiDung;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private final AppContext context = new AppContext();
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Import Order System - ITSS");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        Scene scene = new Scene(new DangNhapBoundary(context, this::showShell).build(), 1120, 720);
        applyStyle(scene);
        stage.setScene(scene);
    }

    private void showShell(NguoiDung nguoiDung) {
        Scene scene = new Scene(new CuaSoChinhBoundary(context, nguoiDung, this::showLogin).build(), 1180, 760);
        applyStyle(scene);
        stage.setScene(scene);
    }

    private void applyStyle(Scene scene) {
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
