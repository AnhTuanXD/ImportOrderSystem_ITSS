package com.itss.importorder.ui;

import com.itss.importorder.AppContext;
import com.itss.importorder.model.User;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class LoginView {
    private final AppContext context;
    private final Consumer<User> onLogin;

    public LoginView(AppContext context, Consumer<User> onLogin) {
        this.context = context;
        this.onLogin = onLogin;
    }

    public Parent build() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(40));

        VBox box = new VBox(16);
        box.setMaxWidth(420);
        box.getStyleClass().add("section");

        Label title = new Label("Hệ thống đặt hàng nhập khẩu");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Đăng nhập theo vai trò để truy cập nghiệp vụ ITSS");

        TextField username = new TextField("sales");
        PasswordField password = new PasswordField();
        password.setText("123456");

        GridPane form = new GridPane();
        form.setVgap(10);
        form.setHgap(10);
        form.addRow(0, new Label("Tài khoản"), username);
        form.addRow(1, new Label("Mật khẩu"), password);

        Button login = UiUtil.primaryButton("Đăng nhập");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(event -> context.getAuthService().login(username.getText(), password.getText())
                .ifPresentOrElse(onLogin, () -> UiUtil.error("Sai tài khoản hoặc mật khẩu.")));
        password.setOnAction(event -> login.fire());

        box.getChildren().addAll(title, subtitle, form, login);
        root.setCenter(box);
        BorderPane.setAlignment(box, Pos.CENTER);
        return root;
    }
}

