package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.TrangThaiDiaDiem;
import com.itss.importorder.util.UiUtil;
import com.itss.importorder.util.ValidationException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DiaDiemNhapBoundary {
    private final AppContext context;
    private final TableView<DiaDiemNhap> table = new TableView<>();

    public DiaDiemNhapBoundary(AppContext context) {
        this.context = context;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Quản lý Site nhập khẩu");
        title.getStyleClass().add("page-title");

        Button add      = UiUtil.primaryButton("Thêm Site");
        Button detail   = new Button("Xem / chỉnh sửa");
        Button activate = new Button("Kích hoạt");
        Button disable  = UiUtil.dangerButton("Vô hiệu hóa");
        HBox toolbar = new HBox(10, add, detail, activate, disable);
        toolbar.getStyleClass().add("toolbar");

        table.getColumns().addAll(
                UiUtil.column("Mã Site",        DiaDiemNhap::getSiteCode,         100),
                UiUtil.column("Tên Site",        DiaDiemNhap::getName,             180),
                UiUtil.column("Tàu",             s -> s.getDeliveryDaysByShip() + " ngày", 90),
                UiUtil.column("Hàng không",      s -> s.getDeliveryDaysByAir()  + " ngày", 110),
                UiUtil.column("Trạng thái",      s -> s.getStatus().getDisplayName(), 140),
                UiUtil.column("Thông tin khác",  DiaDiemNhap::getOtherInformation, 220));
        UiUtil.setupTable(table);

        add.setOnAction(event -> showSiteDialog(null));
        detail.setOnAction(event -> { DiaDiemNhap s = selected(); if (s != null) showSiteDialog(s); });
        activate.setOnAction(event -> changeTrangThai(TrangThaiDiaDiem.ACTIVE));
        disable.setOnAction(event -> changeTrangThai(TrangThaiDiaDiem.DISABLED));

        page.getChildren().addAll(title, toolbar, table);
        refresh();
        return page;
    }

    private void refresh() {
        List<DiaDiemNhap> sites = context.getDiaDiemNhapController().findAll();
        table.setItems(FXCollections.observableArrayList(sites));
    }

    private DiaDiemNhap selected() {
        DiaDiemNhap s = table.getSelectionModel().getSelectedItem();
        if (s == null) UiUtil.error("Vui lòng chọn một Site.");
        return s;
    }

    private void showSiteDialog(DiaDiemNhap diaDiem) {
        TextField code     = new TextField(diaDiem == null ? "" : diaDiem.getSiteCode());
        code.setDisable(diaDiem != null);
        TextField name     = new TextField(diaDiem == null ? "" : diaDiem.getName());
        TextField password = new TextField(diaDiem == null ? "123456" : diaDiem.getPassword());
        TextField ship     = new TextField(diaDiem == null ? "12" : String.valueOf(diaDiem.getDeliveryDaysByShip()));
        TextField air      = new TextField(diaDiem == null ? "4"  : String.valueOf(diaDiem.getDeliveryDaysByAir()));
        TextArea  info     = new TextArea(diaDiem == null ? "" : diaDiem.getOtherInformation());
        info.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setVgap(10);
        form.setHgap(10);
        form.addRow(0, new Label("Mã Site"),              code);
        form.addRow(1, new Label("Tên Site"),              name);
        form.addRow(2, new Label("Mật khẩu"),              password);
        form.addRow(3, new Label("Số ngày bằng tàu"),     ship);
        form.addRow(4, new Label("Số ngày bằng máy bay"), air);
        form.addRow(5, new Label("Thông tin khác"),        info);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(diaDiem == null ? "Thêm Site" : "Chi tiết Site");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK);

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            try {
                String action = diaDiem == null ? "thêm Site" : "cập nhật Site";
                if (!UiUtil.confirm("Xác nhận " + action, "Bạn chắc chắn muốn " + action + "?")) return;
                int shipDays = Integer.parseInt(ship.getText().trim());
                int airDays  = Integer.parseInt(air.getText().trim());
                if (diaDiem == null) {
                    context.getDiaDiemNhapController().add(code.getText().trim(), name.getText().trim(),
                            password.getText(), shipDays, airDays, info.getText());
                } else {
                    context.getDiaDiemNhapController().update(diaDiem, name.getText().trim(), password.getText(),
                            shipDays, airDays, info.getText());
                }
                refresh();
                UiUtil.info("Thành công", "Dữ liệu Site đã được lưu.");
            } catch (NumberFormatException ex) {
                UiUtil.error("Số ngày vận chuyển phải là số nguyên.");
            } catch (ValidationException ex) {
                UiUtil.error(ex.getMessage());
            }
        });
    }

    private void changeTrangThai(TrangThaiDiaDiem trangThai) {
        DiaDiemNhap diaDiem = selected();
        if (diaDiem == null) return;
        if (UiUtil.confirm("Xác nhận thay đổi trạng thái",
                "Bạn chắc chắn muốn chuyển " + diaDiem.getSiteCode() + " sang " + trangThai.getDisplayName() + "?")) {
            context.getDiaDiemNhapController().setTrangThai(diaDiem, trangThai);
            refresh();
            UiUtil.info("Thành công", "Trạng thái Site đã được cập nhật.");
        }
    }
}
