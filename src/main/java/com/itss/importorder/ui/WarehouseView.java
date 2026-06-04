package com.itss.importorder.ui;

import com.itss.importorder.AppContext;
import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.MerchandiseRequest;
import com.itss.importorder.model.WarehouseReport;
import com.itss.importorder.service.ValidationException;
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

public class WarehouseView {
    private final AppContext context;
    private final TableView<ImportRequest> orderTable = new TableView<>();
    private final TableView<WarehouseReport> reportTable = new TableView<>();

    public WarehouseView(AppContext context) {
        this.context = context;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Kiểm tra đơn hàng và báo cáo kho");
        title.getStyleClass().add("page-title");

        Button detail = new Button("Chi tiết đơn hàng");
        Button check = UiUtil.primaryButton("Kiểm tra hoàn tất");
        HBox toolbar = new HBox(10, detail, check);
        toolbar.getStyleClass().add("toolbar");

        orderTable.getColumns().addAll(
                UiUtil.column("Mã đơn", ImportRequest::getRequestCode, 140),
                UiUtil.column("Ngày tạo", request -> request.getCreatedDate().toString(), 120),
                UiUtil.column("Trạng thái", request -> request.getStatus().getDisplayName(), 150),
                UiUtil.column("Tổng SL", request -> String.valueOf(request.getTotalQuantity()), 90));
        UiUtil.setupTable(orderTable);

        reportTable.getColumns().addAll(
                UiUtil.column("Mã báo cáo", WarehouseReport::getReportCode, 130),
                UiUtil.column("Mã đơn", WarehouseReport::getRequestCode, 130),
                UiUtil.column("Người kiểm", WarehouseReport::getChecker, 130),
                UiUtil.column("Kết quả", WarehouseReport::getResult, 160),
                UiUtil.column("Ghi chú", WarehouseReport::getNote, 220));
        UiUtil.setupTable(reportTable);

        detail.setOnAction(event -> showOrderDetail());
        check.setOnAction(event -> showCheckDialog());

        page.getChildren().addAll(title, toolbar, section("Danh sách đơn hàng", orderTable),
                section("Danh sách báo cáo", reportTable));
        refresh();
        return page;
    }

    private VBox section(String title, TableView<?> table) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");
        VBox box = new VBox(8, label, table);
        box.getStyleClass().add("section");
        return box;
    }

    private void refresh() {
        orderTable.setItems(FXCollections.observableArrayList(context.getWarehouseService().findOrdersForChecking()));
        reportTable.setItems(FXCollections.observableArrayList(context.getWarehouseService().findReports()));
    }

    private void showOrderDetail() {
        ImportRequest request = selected();
        if (request == null) {
            return;
        }
        TableView<MerchandiseRequest> items = new TableView<>();
        items.getColumns().addAll(
                UiUtil.column("Mã hàng", MerchandiseRequest::getMerchandiseCode, 130),
                UiUtil.column("Số lượng", item -> String.valueOf(item.getQuantityOrdered()), 100),
                UiUtil.column("Đơn vị", MerchandiseRequest::getUnit, 80),
                UiUtil.column("Ngày mong muốn", item -> item.getDesiredDeliveryDate().toString(), 150));
        UiUtil.setupTable(items);
        items.setItems(FXCollections.observableArrayList(request.getItems()));

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng " + request.getRequestCode());
        dialog.getDialogPane().setContent(items);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showCheckDialog() {
        ImportRequest request = selected();
        if (request == null) {
            return;
        }

        TextField checker = new TextField("warehouse");
        TextField result = new TextField("Đạt");
        TextArea note = new TextArea();
        note.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setVgap(10);
        form.setHgap(10);
        form.addRow(0, new Label("Người kiểm"), checker);
        form.addRow(1, new Label("Kết quả kiểm tra"), result);
        form.addRow(2, new Label("Ghi chú"), note);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Xác nhận kiểm tra đơn hàng");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK);
        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            try {
                context.getWarehouseService().createReport(request, checker.getText(), result.getText(), note.getText());
                refresh();
                UiUtil.info("Kiểm tra hoàn tất", "Báo cáo kiểm hàng đã được lưu vào hệ thống.");
            } catch (ValidationException ex) {
                UiUtil.error(ex.getMessage());
            }
        });
    }

    private ImportRequest selected() {
        ImportRequest request = orderTable.getSelectionModel().getSelectedItem();
        if (request == null) {
            UiUtil.error("Vui lòng chọn một đơn hàng.");
        }
        return request;
    }
}

