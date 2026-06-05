package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.entity.TrangThaiDiaDiem;
import com.itss.importorder.entity.VaiTro;
import com.itss.importorder.util.UiUtil;
import com.itss.importorder.util.ValidationException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class DiaDiemNhapBoundary {
    private final AppContext context;
    private final NguoiDung nguoiDung;
    private final TableView<DiaDiemNhap> siteTable = new TableView<>();
    private final TableView<TonKho>      stockTable = new TableView<>();
    private final Label stockLabel = new Label();
    private final VBox  stockSection = new VBox(8);

    public DiaDiemNhapBoundary(AppContext context, NguoiDung nguoiDung) {
        this.context = context;
        this.nguoiDung = nguoiDung;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Quản lý Site nhập khẩu");
        title.getStyleClass().add("page-title");

        // --- Toolbar site ---
        Button add      = UiUtil.primaryButton("Thêm Site");
        Button edit     = new Button("Xem / chỉnh sửa Site");
        Button activate = new Button("Kích hoạt");
        Button disable  = UiUtil.dangerButton("Vô hiệu hóa");
        HBox toolbar = new HBox(10, add, edit, activate, disable);
        toolbar.getStyleClass().add("toolbar");

        // --- Bảng Site ---
        siteTable.getColumns().addAll(
                UiUtil.column("Mã Site",       DiaDiemNhap::getSiteCode,              90),
                UiUtil.column("Tên Site",       DiaDiemNhap::getName,                 180),
                UiUtil.column("Tài khoản",      DiaDiemNhap::getTaiKhoan,             130),
                UiUtil.column("Tàu",            s -> s.getDeliveryDaysByShip() + " ngày", 80),
                UiUtil.column("Hàng không",     s -> s.getDeliveryDaysByAir()  + " ngày", 100),
                UiUtil.column("Trạng thái",     s -> s.getStatus().getDisplayName(),   130),
                UiUtil.column("Thông tin khác", DiaDiemNhap::getOtherInformation,     200));
        UiUtil.setupTable(siteTable);
        VBox.setVgrow(siteTable, Priority.ALWAYS);

        // --- Phần tồn kho (ẩn mặc định) ---
        stockLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        Button addStock    = UiUtil.primaryButton("Thêm sản phẩm");
        Button editStock   = new Button("Sửa sản phẩm");
        Button deleteStock = UiUtil.dangerButton("Xóa sản phẩm");
        Button refreshStock = new Button("Làm mới tồn kho");
        
        HBox stockToolbar;
        if (nguoiDung != null && nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER) {
            stockToolbar = new HBox(10, refreshStock);
        } else {
            stockToolbar = new HBox(10, addStock, editStock, deleteStock, refreshStock);
        }

        stockTable.getColumns().addAll(
                UiUtil.column("Mã mặt hàng",  TonKho::getMerchandiseCode,                    130),
                UiUtil.column("Tên mặt hàng", TonKho::getMerchandiseName,                    220),
                UiUtil.column("Đơn vị tính",  TonKho::getUnit,                                90),
                UiUtil.column("Số lượng",     t -> String.valueOf(t.getInStockQuantity()),    100));
        UiUtil.setupTable(stockTable);
        stockTable.setPrefHeight(200);

        stockSection.setPadding(new Insets(8, 0, 0, 0));
        stockSection.getChildren().addAll(stockLabel, stockToolbar, stockTable);
        stockSection.setVisible(false);
        stockSection.setManaged(false);

        // --- Events ---
        add.setOnAction(e      -> showSiteDialog(null));
        edit.setOnAction(e     -> { DiaDiemNhap s = selectedSite(); if (s != null) showSiteDialog(s); });
        activate.setOnAction(e -> changeTrangThai(TrangThaiDiaDiem.ACTIVE));
        disable.setOnAction(e  -> changeTrangThai(TrangThaiDiaDiem.DISABLED));

        siteTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                stockLabel.setText("Danh sách sản phẩm và tồn kho của Site: " + sel.getSiteCode());
                loadStock(sel.getSiteCode());
                stockSection.setVisible(true);
                stockSection.setManaged(true);
            } else {
                stockSection.setVisible(false);
                stockSection.setManaged(false);
            }
        });

        addStock.setOnAction(e -> {
            DiaDiemNhap sel = selectedSite(); if (sel == null) return;
            showStockDialog(sel.getSiteCode(), null);
        });
        editStock.setOnAction(e -> {
            TonKho sel = stockTable.getSelectionModel().getSelectedItem();
            if (sel == null) { UiUtil.error("Vui lòng chọn một sản phẩm."); return; }
            showStockDialog(sel.getSiteCode(), sel);
        });
        deleteStock.setOnAction(e -> deleteSelectedStock());
        refreshStock.setOnAction(e -> {
            DiaDiemNhap sel = selectedSite(); if (sel != null) loadStock(sel.getSiteCode());
        });

        page.getChildren().addAll(title, toolbar, siteTable, stockSection);
        refreshSites();
        return page;
    }

    private void refreshSites() {
        siteTable.setItems(FXCollections.observableArrayList(
                context.getDiaDiemNhapController().findAll()));
    }

    private void loadStock(String siteCode) {
        stockTable.setItems(FXCollections.observableArrayList(
                context.getTonKhos(siteCode)));
    }

    private DiaDiemNhap selectedSite() {
        DiaDiemNhap s = siteTable.getSelectionModel().getSelectedItem();
        if (s == null) UiUtil.error("Vui lòng chọn một Site.");
        return s;
    }

    private void showSiteDialog(DiaDiemNhap diaDiem) {
        TextField code = new TextField(diaDiem == null ? "" : diaDiem.getSiteCode());
        code.setDisable(diaDiem != null);
        TextField name = new TextField(diaDiem == null ? "" : diaDiem.getName());
        TextField ship = new TextField(diaDiem == null ? "10" : String.valueOf(diaDiem.getDeliveryDaysByShip()));
        TextField air  = new TextField(diaDiem == null ? "3"  : String.valueOf(diaDiem.getDeliveryDaysByAir()));
        TextArea  info = new TextArea(diaDiem == null ? "" : diaDiem.getOtherInformation());
        info.setPrefRowCount(3);

        // ComboBox tài khoản: chỉ lấy IMPORT_SITE users chưa được gán cho site khác
        ComboBox<NguoiDung> taiKhoanBox = new ComboBox<>();
        taiKhoanBox.setConverter(new StringConverter<>() {
            @Override public String toString(NguoiDung u) { return u == null ? "" : u.getUsername(); }
            @Override public NguoiDung fromString(String s) { return null; }
        });
        java.util.Set<String> assignedAccounts = context.getDiaDiemNhapController().findAll().stream()
                .filter(s -> diaDiem == null || !s.getSiteCode().equals(diaDiem.getSiteCode()))
                .map(DiaDiemNhap::getTaiKhoan)
                .collect(java.util.stream.Collectors.toSet());
        List<NguoiDung> siteUsers = context.getQuanTriTaiKhoanController().findAll().stream()
                .filter(u -> u.getVaiTro() == VaiTro.IMPORT_SITE)
                .filter(u -> !assignedAccounts.contains(u.getUsername()))
                .collect(java.util.stream.Collectors.toList());
        taiKhoanBox.setItems(FXCollections.observableArrayList(siteUsers));
        if (diaDiem != null) {
            siteUsers.stream()
                    .filter(u -> u.getUsername().equals(diaDiem.getTaiKhoan()))
                    .findFirst()
                    .ifPresent(taiKhoanBox::setValue);
        }
        taiKhoanBox.setPrefWidth(200);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setVgap(10);
        form.setHgap(12);

        javafx.scene.layout.ColumnConstraints labelCol = new javafx.scene.layout.ColumnConstraints();
        labelCol.setMinWidth(180);
        javafx.scene.layout.ColumnConstraints fieldCol = new javafx.scene.layout.ColumnConstraints();
        fieldCol.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        fieldCol.setFillWidth(true);
        form.getColumnConstraints().addAll(labelCol, fieldCol);

        code.setMaxWidth(Double.MAX_VALUE);
        name.setMaxWidth(Double.MAX_VALUE);
        ship.setMaxWidth(Double.MAX_VALUE);
        air.setMaxWidth(Double.MAX_VALUE);
        taiKhoanBox.setMaxWidth(Double.MAX_VALUE);
        info.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Mã Site *"),              code);
        form.addRow(1, new Label("Tên Site *"),              name);
        form.addRow(2, new Label("Tài khoản *"),             taiKhoanBox);
        form.addRow(3, new Label("Số ngày bằng tàu *"),     ship);
        form.addRow(4, new Label("Số ngày bằng máy bay *"), air);
        form.addRow(5, new Label("Thông tin khác"),          info);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(diaDiem == null ? "Thêm Site" : "Chỉnh sửa Site");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK);

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            try {
                if (taiKhoanBox.getValue() == null) { UiUtil.error("Vui lòng chọn tài khoản."); return; }
                String action = diaDiem == null ? "thêm Site" : "cập nhật Site";
                if (!UiUtil.confirm("Xác nhận " + action, "Bạn chắc chắn muốn " + action + "?")) return;
                int shipDays = Integer.parseInt(ship.getText().trim());
                int airDays  = Integer.parseInt(air.getText().trim());
                String tk    = taiKhoanBox.getValue().getUsername();
                if (diaDiem == null) {
                    context.getDiaDiemNhapController().add(
                            code.getText().trim(), name.getText().trim(), tk, shipDays, airDays, info.getText());
                } else {
                    context.getDiaDiemNhapController().update(
                            diaDiem, name.getText().trim(), tk, shipDays, airDays, info.getText());
                }
                refreshSites();
                UiUtil.info("Thành công", "Dữ liệu Site đã được lưu.");
            } catch (NumberFormatException ex) {
                UiUtil.error("Số ngày vận chuyển phải là số nguyên.");
            } catch (ValidationException ex) {
                UiUtil.error(ex.getMessage());
            }
        });
    }

    private void showStockDialog(String siteCode, TonKho existing) {
        TextField code     = new TextField(existing == null ? "" : existing.getMerchandiseCode());
        code.setDisable(existing != null);
        TextField itemName = new TextField(existing == null ? "" : existing.getMerchandiseName());
        TextField quantity = new TextField(existing == null ? "0" : String.valueOf(existing.getInStockQuantity()));
        TextField unit     = new TextField(existing == null ? "pcs" : existing.getUnit());

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setVgap(10);
        form.setHgap(12);

        javafx.scene.layout.ColumnConstraints lc = new javafx.scene.layout.ColumnConstraints();
        lc.setMinWidth(130);
        javafx.scene.layout.ColumnConstraints fc = new javafx.scene.layout.ColumnConstraints();
        fc.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        fc.setFillWidth(true);
        form.getColumnConstraints().addAll(lc, fc);

        code.setMaxWidth(Double.MAX_VALUE);
        itemName.setMaxWidth(Double.MAX_VALUE);
        unit.setMaxWidth(Double.MAX_VALUE);
        quantity.setMaxWidth(Double.MAX_VALUE);

        form.addRow(0, new Label("Mã mặt hàng *"),  code);
        form.addRow(1, new Label("Tên mặt hàng *"),  itemName);
        form.addRow(2, new Label("Đơn vị tính *"),   unit);
        form.addRow(3, new Label("Số lượng *"),       quantity);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK);

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            try {
                if (code.getText().isBlank())     { UiUtil.error("Mã mặt hàng không được để trống."); return; }
                if (itemName.getText().isBlank())  { UiUtil.error("Tên mặt hàng không được để trống."); return; }
                if (unit.getText().isBlank())      { UiUtil.error("Đơn vị không được để trống."); return; }
                int qty = Integer.parseInt(quantity.getText().trim());
                context.saveTonKho(new TonKho(siteCode, code.getText().trim(),
                        itemName.getText().trim(), qty, unit.getText().trim()));
                loadStock(siteCode);
                UiUtil.info("Thành công", "Sản phẩm đã được lưu.");
            } catch (NumberFormatException ex) {
                UiUtil.error("Số lượng phải là số nguyên.");
            } catch (Exception ex) {
                UiUtil.error("Lỗi: " + ex.getMessage());
            }
        });
    }

    private void deleteSelectedStock() {
        DiaDiemNhap site  = selectedSite(); if (site == null) return;
        TonKho      stock = stockTable.getSelectionModel().getSelectedItem();
        if (stock == null) { UiUtil.error("Vui lòng chọn sản phẩm cần xóa."); return; }
        if (!UiUtil.confirm("Xác nhận xóa", "Xóa sản phẩm " + stock.getMerchandiseCode() + "?")) return;
        try {
            context.deleteTonKho(site.getSiteCode(), stock.getMerchandiseCode());
            loadStock(site.getSiteCode());
            UiUtil.info("Thành công", "Sản phẩm đã được xóa.");
        } catch (Exception ex) {
            UiUtil.error("Lỗi: " + ex.getMessage());
        }
    }

    private void changeTrangThai(TrangThaiDiaDiem trangThai) {
        DiaDiemNhap diaDiem = selectedSite(); if (diaDiem == null) return;
        if (UiUtil.confirm("Xác nhận thay đổi trạng thái",
                "Chuyển " + diaDiem.getSiteCode() + " sang " + trangThai.getDisplayName() + "?")) {
            context.getDiaDiemNhapController().setTrangThai(diaDiem, trangThai);
            refreshSites();
            UiUtil.info("Thành công", "Trạng thái Site đã được cập nhật.");
        }
    }
}
