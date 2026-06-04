package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.TrangThaiYeuCau;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.util.UiUtil;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class XacNhanDonHangBoundary {
    private final AppContext context;
    private final NguoiDung nguoiDung;
    private final TableView<YeuCauNhapHang> table = new TableView<>();

    public XacNhanDonHangBoundary(AppContext context, NguoiDung nguoiDung) {
        this.context    = context;
        this.nguoiDung  = nguoiDung;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Xác nhận đơn hàng");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Các đơn hàng đã được lập phương án và phân bổ đến Site của bạn.");
        subtitle.setStyle("-fx-text-fill: #666;");

        Button detail  = new Button("Xem chi tiết");
        Button confirm = UiUtil.primaryButton("Xác nhận đơn hàng");
        Button cancel  = UiUtil.dangerButton("Hủy đơn hàng");
        HBox toolbar   = new HBox(10, detail, confirm, cancel);
        toolbar.getStyleClass().add("toolbar");

        table.getColumns().addAll(
                UiUtil.column("Mã đơn hàng",  YeuCauNhapHang::getRequestCode,               140),
                UiUtil.column("Ngày tạo",      r -> r.getCreatedDate().toString(),             120),
                UiUtil.column("Người tạo",     YeuCauNhapHang::getCreatedBy,                  120),
                UiUtil.column("Trạng thái",    r -> r.getStatus().getDisplayName(),            160),
                UiUtil.column("Tổng SL",       r -> String.valueOf(r.getTotalQuantity()),        90));
        UiUtil.setupTable(table);
        table.setPlaceholder(new Label("Không có đơn hàng nào đang chờ xác nhận."));

        detail.setOnAction(e  -> showDetail(selected()));
        confirm.setOnAction(e -> confirmOrder());
        cancel.setOnAction(e  -> cancelOrder());

        page.getChildren().addAll(title, subtitle, toolbar, table);
        refresh();
        return page;
    }

    private void refresh() {
        String siteCode = resolveSiteCode();
        List<YeuCauNhapHang> list = context.getYeuCauNhapHangController()
                .findPlanningForSite(siteCode);
        table.setItems(FXCollections.observableArrayList(list));
    }

    private String resolveSiteCode() {
        return context.getDiaDiemNhapController()
                .findByTaiKhoan(nguoiDung.getUsername())
                .map(DiaDiemNhap::getSiteCode)
                .orElse(nguoiDung.getUsername());
    }

    private YeuCauNhapHang selected() {
        YeuCauNhapHang r = table.getSelectionModel().getSelectedItem();
        if (r == null) UiUtil.error("Vui lòng chọn một đơn hàng.");
        return r;
    }

    private void showDetail(YeuCauNhapHang ycnh) {
        if (ycnh == null) return;
        TableView<ChiTietHangHoa> items = new TableView<>();
        items.getColumns().addAll(
                UiUtil.column("Mã hàng",    ChiTietHangHoa::getMerchandiseCode,              120),
                UiUtil.column("Tên hàng",   ChiTietHangHoa::getMerchandiseName,              200),
                UiUtil.column("Số lượng",   i -> String.valueOf(i.getQuantityOrdered()),       90),
                UiUtil.column("Đơn vị",     ChiTietHangHoa::getUnit,                          80),
                UiUtil.column("Ngày cần",   i -> i.getDesiredDeliveryDate().toString(),       130));
        UiUtil.setupTable(items);
        items.setItems(FXCollections.observableArrayList(ycnh.getItems()));
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng " + ycnh.getRequestCode());
        dialog.getDialogPane().setContent(items);
        dialog.getDialogPane().setPrefWidth(650);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void confirmOrder() {
        YeuCauNhapHang ycnh = selected();
        if (ycnh == null) return;
        if (!UiUtil.confirm("Xác nhận đơn hàng",
                "Xác nhận đặt hàng cho " + ycnh.getRequestCode() + "?")) return;
        context.getYeuCauNhapHangController().confirmOrderForSite(ycnh, resolveSiteCode());
        refresh();
        UiUtil.info("Thành công", "Đơn hàng " + ycnh.getRequestCode() + " đã chuyển sang Đã đặt hàng và cập nhật tồn kho.");
    }

    private void cancelOrder() {
        YeuCauNhapHang ycnh = selected();
        if (ycnh == null) return;
        if (!UiUtil.confirm("Hủy đơn hàng",
                "Bạn chắc chắn muốn hủy đơn hàng " + ycnh.getRequestCode() + "?")) return;
        context.getYeuCauNhapHangController().updateTrangThai(ycnh, TrangThaiYeuCau.CANCELLED);
        refresh();
        UiUtil.info("Đã hủy", "Đơn hàng " + ycnh.getRequestCode() + " đã bị hủy.");
    }
}
