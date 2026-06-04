package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.VaiTro;
import com.itss.importorder.util.UiUtil;
import com.itss.importorder.util.ValidationException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class QuanTriTaiKhoanBoundary {
    private final AppContext context;
    private final NguoiDung nguoiDung;
    private final TableView<NguoiDung> table = new TableView<>();
    private final TextField usernameField   = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<VaiTro> roleBox  = new ComboBox<>();
    private NguoiDung selected = null;

    public QuanTriTaiKhoanBoundary(AppContext context, NguoiDung nguoiDung) {
        this.context    = context;
        this.nguoiDung  = nguoiDung;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Quản trị tài khoản");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Admin có thể tạo, xóa, chỉnh sửa tài khoản và phân quyền role cho người dùng.");
        subtitle.setStyle("-fx-text-fill: #666;");

        Button refreshBtn = new Button("Làm mới");
        Button createBtn  = UiUtil.primaryButton("Tạo mới");
        Button saveBtn    = UiUtil.primaryButton("Lưu tài khoản");
        Button deleteBtn  = UiUtil.dangerButton("Xóa tài khoản");
        HBox toolbar = new HBox(10, refreshBtn, createBtn, saveBtn, deleteBtn);
        toolbar.getStyleClass().add("toolbar");

        // Form bên trái
        usernameField.setPromptText("Nhập username");
        passwordField.setPromptText("Nhập password");
        roleBox.setItems(FXCollections.observableArrayList(VaiTro.values()));
        roleBox.setConverter(new StringConverter<>() {
            @Override public String toString(VaiTro v) { return v == null ? "" : v.getDisplayName(); }
            @Override public VaiTro fromString(String s) { return null; }
        });
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Label formTitle = new Label("Thông tin tài khoản");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        Label hint = new Label("* Chọn một dòng trong bảng để chỉnh sửa, hoặc bấm Tạo mới để thêm.");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 11; -fx-wrap-text: true;");

        VBox form = new VBox(10, formTitle,
                fieldRow("Tài khoản *", usernameField),
                fieldRow("Mật khẩu *",  passwordField),
                fieldRow("Role *",       roleBox),
                hint);
        form.setPadding(new Insets(16));
        form.setPrefWidth(280);
        form.setStyle("-fx-border-color: #ddd; -fx-border-radius: 6; " +
                      "-fx-background-color: white; -fx-background-radius: 6;");

        // Bảng bên phải
        table.getColumns().addAll(
                UiUtil.column("Tài khoản", NguoiDung::getUsername,                        150),
                UiUtil.column("Role",      u -> u.getVaiTro().getDisplayName(),            200),
                UiUtil.column("Mật khẩu", u -> "********",                                120));
        UiUtil.setupTable(table);

        Label tableTitle = new Label("Danh sách tài khoản");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        VBox tableBox = new VBox(8, tableTitle, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        HBox content = new HBox(16, form, tableBox);
        VBox.setVgrow(content, Priority.ALWAYS);

        // Sự kiện
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            selected = val;
            if (val != null) {
                usernameField.setText(val.getUsername());
                usernameField.setDisable(true);
                passwordField.setText(val.getPassword());
                roleBox.setValue(val.getVaiTro());
            }
        });

        refreshBtn.setOnAction(e -> clearForm());
        createBtn.setOnAction(e  -> createAccount());
        saveBtn.setOnAction(e    -> saveAccount());
        deleteBtn.setOnAction(e  -> deleteAccount());

        page.getChildren().addAll(title, subtitle, toolbar, content);
        loadTable();
        return page;
    }

    private VBox fieldRow(String label, Region control) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 12;");
        control.setMaxWidth(Double.MAX_VALUE);
        return new VBox(4, lbl, control);
    }

    private void loadTable() {
        List<NguoiDung> list = context.getQuanTriTaiKhoanController().findAll();
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void clearForm() {
        usernameField.setText("");
        usernameField.setDisable(false);
        passwordField.setText("");
        roleBox.setValue(null);
        table.getSelectionModel().clearSelection();
        selected = null;
    }

    private void createAccount() {
        try {
            if (roleBox.getValue() == null) { UiUtil.error("Vui lòng chọn Role."); return; }
            context.getQuanTriTaiKhoanController().create(
                    usernameField.getText().trim(),
                    passwordField.getText().trim(),
                    roleBox.getValue());
            loadTable();
            clearForm();
            UiUtil.info("Thành công", "Tài khoản đã được tạo.");
        } catch (ValidationException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void saveAccount() {
        if (selected == null) { UiUtil.error("Vui lòng chọn tài khoản cần chỉnh sửa."); return; }
        try {
            if (roleBox.getValue() == null) { UiUtil.error("Vui lòng chọn Role."); return; }
            context.getQuanTriTaiKhoanController().update(
                    selected.getUsername(),
                    passwordField.getText().trim(),
                    roleBox.getValue());
            loadTable();
            clearForm();
            UiUtil.info("Thành công", "Tài khoản đã được cập nhật.");
        } catch (ValidationException ex) {
            UiUtil.error(ex.getMessage());
        }
    }

    private void deleteAccount() {
        if (selected == null) { UiUtil.error("Vui lòng chọn tài khoản cần xóa."); return; }
        if (!UiUtil.confirm("Xác nhận xóa",
                "Bạn chắc chắn muốn xóa tài khoản \"" + selected.getUsername() + "\"?")) return;
        try {
            context.getQuanTriTaiKhoanController().delete(
                    selected.getUsername(), nguoiDung.getUsername());
            loadTable();
            clearForm();
            UiUtil.info("Thành công", "Tài khoản đã được xóa.");
        } catch (ValidationException ex) {
            UiUtil.error(ex.getMessage());
        }
    }
}
