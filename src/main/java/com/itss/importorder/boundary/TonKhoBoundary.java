package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.util.UiUtil;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TonKhoBoundary {
    private final AppContext context;
    private final NguoiDung nguoiDung;
    private final TableView<TonKho> table = new TableView<>();
    private final HBox statsBar = new HBox(12);

    public TonKhoBoundary(AppContext context, NguoiDung nguoiDung) {
        this.context = context;
        this.nguoiDung = nguoiDung;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Quản lý mặt hàng Site nhập khẩu");
        title.getStyleClass().add("page-title");

        Button add = UiUtil.primaryButton("+ Thêm mặt hàng");
        HBox toolbar = new HBox(10, add);
        toolbar.getStyleClass().add("toolbar");

        table.getColumns().addAll(
                UiUtil.column("Mã hàng",         TonKho::getMerchandiseCode,                            130),
                UiUtil.column("Tên mặt hàng",    TonKho::getMerchandiseName,                            200),
                UiUtil.column("Đơn vị",           TonKho::getUnit,                                       90),
                UiUtil.column("Số lượng tồn",    r -> String.valueOf(r.getInStockQuantity()),            110),
                UiUtil.column("Trạng thái",       r -> r.getInStockQuantity() < 100 ? "⚠ Tồn kho thấp" : "Bình thường", 150));
        UiUtil.setupTable(table);
        table.setPlaceholder(new Label("Chưa có mặt hàng nào trong kho."));

        Button edit   = new Button("Cập nhật số lượng");
        Button delete = UiUtil.dangerButton("Xóa mặt hàng");
        HBox actions  = new HBox(10, edit, delete);

        add.setOnAction(e -> showTonKhoDialog(null));
        edit.setOnAction(e -> {
            TonKho selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) { UiUtil.error("Vui lòng chọn một mặt hàng."); return; }
            showTonKhoDialog(selected);
        });
        delete.setOnAction(e -> deleteSelected());

        page.getChildren().addAll(title, statsBar, toolbar, table, actions);
        refresh();
        return page;
    }

    private String resolveSiteCode() {
        return context.getDiaDiemNhapController()
                .findByTaiKhoan(nguoiDung.getUsername())
                .map(DiaDiemNhap::getSiteCode)
                .orElse(nguoiDung.getUsername());
    }

    private void refresh() {
        List<TonKho> records = context.getTonKhos(resolveSiteCode());
        table.setItems(FXCollections.observableArrayList(records));
        updateStats(records);
    }

    private void updateStats(List<TonKho> records) {
        int total    = records.size();
        double avg   = records.stream().mapToInt(TonKho::getInStockQuantity).average().orElse(0);
        long lowStock = records.stream().filter(r -> r.getInStockQuantity() < 100).count();

        statsBar.getChildren().setAll(
                buildStatCard("Tổng mặt hàng",         String.valueOf(total),            "#1976D2"),
                buildStatCard("Tồn kho trung bình",     String.format("%.0f", avg),       "#4CAF50"),
                buildStatCard("Cảnh báo tồn kho thấp",  String.valueOf(lowStock),          "#FF9800"));
    }

    private VBox buildStatCard(String label, String value, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8;");
        card.setPrefWidth(180);
        Label titleLabel = new Label(label);
        titleLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private void showTonKhoDialog(TonKho existing) {
        TextField code     = new TextField(existing == null ? "" : existing.getMerchandiseCode());
        code.setDisable(existing != null);
        TextField itemName = new TextField(existing == null ? "" : existing.getMerchandiseName());
        TextField quantity = new TextField(existing == null ? "0" : String.valueOf(existing.getInStockQuantity()));
        TextField unit     = new TextField(existing == null ? "pcs" : existing.getUnit());

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setVgap(10);
        form.setHgap(10);
        form.addRow(0, new Label("Mã hàng *"),       code);
        form.addRow(1, new Label("Tên mặt hàng *"),  itemName);
        form.addRow(2, new Label("Số lượng *"),       quantity);
        form.addRow(3, new Label("Đơn vị *"),         unit);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm mặt hàng mới" : "Cập nhật mặt hàng");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(btn -> btn == ButtonType.OK);

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            try {
                int qty = Integer.parseInt(quantity.getText().trim());
                if (code.getText().trim().isEmpty()) { UiUtil.error("Mã hàng không được để trống."); return; }
                if (itemName.getText().trim().isEmpty()) { UiUtil.error("Tên mặt hàng không được để trống."); return; }
                context.saveTonKho(new TonKho(resolveSiteCode(), code.getText().trim(),
                        itemName.getText().trim(), qty, unit.getText().trim()));
                refresh();
                UiUtil.info("Thành công", existing == null ? "Mặt hàng đã được thêm vào kho." : "Số lượng tồn kho đã được cập nhật.");
            } catch (NumberFormatException ex) {
                UiUtil.error("Số lượng phải là số nguyên.");
            } catch (Exception ex) {
                UiUtil.error("Lỗi: " + ex.getMessage());
            }
        });
    }

    private void deleteSelected() {
        TonKho record = table.getSelectionModel().getSelectedItem();
        if (record == null) { UiUtil.error("Vui lòng chọn một mặt hàng."); return; }
        if (UiUtil.confirm("Xác nhận xóa",
                "Bạn chắc chắn muốn xóa mặt hàng " + record.getMerchandiseCode() + " khỏi kho?")) {
            try {
                context.deleteTonKho(record.getSiteCode(), record.getMerchandiseCode());
                refresh();
                UiUtil.info("Thành công", "Mặt hàng đã được xóa khỏi kho.");
            } catch (Exception ex) {
                UiUtil.error("Lỗi: " + ex.getMessage());
            }
        }
    }
}
