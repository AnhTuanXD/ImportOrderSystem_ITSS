package com.itss.importorder.ui;

import com.itss.importorder.AppContext;
import com.itss.importorder.model.Role;
import com.itss.importorder.model.User;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ShellView {
    private final AppContext context;
    private final User user;
    private final Runnable onLogout;
    private final BorderPane root = new BorderPane();

    public ShellView(AppContext context, User user, Runnable onLogout) {
        this.context = context;
        this.user = user;
        this.onLogout = onLogout;
    }

    public Parent build() {
        root.setLeft(buildSidebar());
        showHome();
        return root;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("sidebar");

        Label title = new Label("Import Order");
        title.getStyleClass().add("sidebar-title");
        Label role = new Label(user.getRole().getDisplayName());
        role.getStyleClass().add("sidebar-subtitle");

        Button home = navButton("Trang chủ", this::showHome);
        Button requests = navButton("Yêu cầu nhập hàng", () ->
                root.setCenter(new RequestsView(context, user).build()));
        Button sites = navButton("Quản lý Site", () ->
                root.setCenter(new SitesView(context).build()));
        Button planning = navButton("Lập phương án", () ->
                root.setCenter(new PlanningView(context).build()));
        Button warehouse = navButton("Quản lý kho", () ->
                root.setCenter(new WarehouseView(context).build()));
        Button logout = navButton("Đăng xuất", onLogout);

        sidebar.getChildren().addAll(title, role, home);
        if (user.getRole() == Role.SALES || user.getRole() == Role.OVERSEAS_ORDER) {
            sidebar.getChildren().add(requests);
        }
        if (user.getRole() == Role.OVERSEAS_ORDER || user.getRole() == Role.IMPORT_SITE) {
            sidebar.getChildren().add(sites);
        }
        if (user.getRole() == Role.OVERSEAS_ORDER) {
            sidebar.getChildren().add(planning);
        }
        if (user.getRole() == Role.WAREHOUSE || user.getRole() == Role.OVERSEAS_ORDER) {
            sidebar.getChildren().add(warehouse);
        }
        sidebar.getChildren().add(logout);
        return sidebar;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void showHome() {
        VBox page = new VBox(14);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(18));
        Label title = new Label("Trang chủ");
        title.getStyleClass().add("page-title");
        Label summary = new Label("Chọn chức năng ở thanh bên để xử lý quy trình đặt hàng nhập khẩu.");
        Label flow = new Label("Luồng chính: Bán hàng tạo yêu cầu -> Đặt hàng quốc tế lập phương án -> Kho kiểm hàng và báo cáo.");
        page.getChildren().addAll(title, summary, flow);
        root.setCenter(page);
    }
}

