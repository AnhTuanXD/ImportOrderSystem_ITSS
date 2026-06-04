package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.BaoCaoKho;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.VaiTro;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.util.UiUtil;
import com.itss.importorder.util.ValidationException;
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

public class KiemHangBoundary {
    private final AppContext context;
    private final NguoiDung nguoiDung;
    private final TableView<YeuCauNhapHang> orderTable  = new TableView<>();
    private final TableView<BaoCaoKho>      reportTable = new TableView<>();

    public KiemHangBoundary(AppContext context, NguoiDung nguoiDung) {
        this.context = context;
        this.nguoiDung = nguoiDung;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Kiểm tra đơn hàng và báo cáo kho");
        title.getStyleClass().add("page-title");

        Button detail = new Button("Chi tiết đơn hàng");
        Button check  = UiUtil.primaryButton("Kiểm tra hoàn tất");
        HBox toolbar;
        if (nguoiDung != null && nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER) {
            toolbar = new HBox(10, detail);
        } else {
            toolbar = new HBox(10, detail, check);
        }
        toolbar.getStyleClass().add("toolbar");

        orderTable.getColumns().addAll(
                UiUtil.column("Mã đơn",    YeuCauNhapHang::getRequestCode,                             140),
                UiUtil.column("Ngày tạo",  r -> r.getCreatedDate().toString(),                          120),
                UiUtil.column("Trạng thái",r -> r.getStatus().getDisplayName(),                         150),
                UiUtil.column("Tổng SL",   r -> String.valueOf(r.getTotalQuantity()),                    90));
        UiUtil.setupTable(orderTable);

        reportTable.getColumns().addAll(
                UiUtil.column("Mã báo cáo", BaoCaoKho::getReportCode,  130),
                UiUtil.column("Mã đơn",     BaoCaoKho::getRequestCode, 130),
                UiUtil.column("Người kiểm", BaoCaoKho::getChecker,     130),
                UiUtil.column("Kết quả",    BaoCaoKho::getResult,      160),
                UiUtil.column("Ghi chú",    BaoCaoKho::getNote,        220));
        UiUtil.setupTable(reportTable);

        detail.setOnAction(event -> showOrderDetail());
        check.setOnAction(event -> showCheckDialog());

        page.getChildren().addAll(title, toolbar,
                section("Danh sách đơn hàng", orderTable),
                section("Danh sách báo cáo",  reportTable));
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
        orderTable.setItems(FXCollections.observableArrayList(context.getKiemHangController().findOrdersForChecking()));
        reportTable.setItems(FXCollections.observableArrayList(context.getKiemHangController().findReports()));
    }

    private void showOrderDetail() {
        YeuCauNhapHang ycnh = selected();
        if (ycnh == null) return;

        TableView<ChiTietHangHoa> items = new TableView<>();
        items.getColumns().addAll(
                UiUtil.column("Mã hàng",       ChiTietHangHoa::getMerchandiseCode,                  130),
                UiUtil.column("Số lượng",       i -> String.valueOf(i.getQuantityOrdered()),          100),
                UiUtil.column("Đơn vị",         ChiTietHangHoa::getUnit,                             80),
                UiUtil.column("Ngày mong muốn", i -> i.getDesiredDeliveryDate().toString(),           150));
        UiUtil.setupTable(items);
        items.setItems(FXCollections.observableArrayList(ycnh.getItems()));

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng " + ycnh.getRequestCode());
        dialog.getDialogPane().setContent(items);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showCheckDialog() {
        if (nguoiDung != null && nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER) {
            UiUtil.error("Bạn không có quyền thực hiện chức năng này.");
            return;
        }
        YeuCauNhapHang ycnh = selected();
        if (ycnh == null) return;

        TextField checker = new TextField("warehouse");
        TextField result  = new TextField("Đạt");
        TextArea  note    = new TextArea();
        note.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setVgap(10);
        form.setHgap(10);
        form.addRow(0, new Label("Người kiểm"),       checker);
        form.addRow(1, new Label("Kết quả kiểm tra"), result);
        form.addRow(2, new Label("Ghi chú"),           note);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Xác nhận kiểm tra đơn hàng");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK);
        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            try {
                context.getKiemHangController().createReport(ycnh, checker.getText(), result.getText(), note.getText());
                refresh();
                UiUtil.info("Kiểm tra hoàn tất", "Báo cáo kiểm hàng đã được lưu vào hệ thống.");
            } catch (ValidationException ex) {
                UiUtil.error(ex.getMessage());
            }
        });
    }

    private YeuCauNhapHang selected() {
        YeuCauNhapHang r = orderTable.getSelectionModel().getSelectedItem();
        if (r == null) UiUtil.error("Vui lòng chọn một đơn hàng.");
        return r;
    }
}
