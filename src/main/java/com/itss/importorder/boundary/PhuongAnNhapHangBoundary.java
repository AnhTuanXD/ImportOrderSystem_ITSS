package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.controller.PhuongAnController;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.PhanBo;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.TrangThaiYeuCau;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.util.UiUtil;
import com.itss.importorder.util.ValidationException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class PhuongAnNhapHangBoundary {
    private final AppContext context;
    private final ComboBox<YeuCauNhapHang>                  requestBox     = new ComboBox<>();
    private final ComboBox<ChiTietHangHoa>                  itemBox        = new ComboBox<>();
    private final TableView<PhuongAnController.UngVienPreview> ungVienTable = new TableView<>();
    private final TableView<PhanBo>                         allocationTable = new TableView<>();

    public PhuongAnNhapHangBoundary(AppContext context) {
        this.context = context;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Lập phương án nhập hàng từ Sites");
        title.getStyleClass().add("page-title");

        requestBox.setPrefWidth(260);
        requestBox.setConverter(new StringConverter<>() {
            @Override public String toString(YeuCauNhapHang r) {
                return r == null ? "" : r.getRequestCode() + " - " + r.getStatus().getDisplayName();
            }
            @Override public YeuCauNhapHang fromString(String value) { return null; }
        });
        itemBox.setPrefWidth(260);
        itemBox.setConverter(new StringConverter<>() {
            @Override public String toString(ChiTietHangHoa item) {
                return item == null ? "" : item.getMerchandiseCode() + " / " + item.getQuantityOrdered() + " " + item.getUnit();
            }
            @Override public ChiTietHangHoa fromString(String value) { return null; }
        });

        Button preview = new Button("Xem Site đáp ứng");
        Button create  = UiUtil.primaryButton("Tạo phương án");
        HBox toolbar = new HBox(10, new Label("Yêu cầu"), requestBox, new Label("Mặt hàng"), itemBox, preview, create);
        toolbar.getStyleClass().add("toolbar");

        ungVienTable.getColumns().addAll(
                UiUtil.column("Mã Site",    PhuongAnController.UngVienPreview::getSiteCode,          100),
                UiUtil.column("Tên Site",   PhuongAnController.UngVienPreview::getSiteName,          180),
                UiUtil.column("Tồn kho",    c -> c.getInStockQuantity() + " " + c.getUnit(),         100),
                UiUtil.column("Phương tiện",c -> c.getPhuongThucGiaoHang().getDisplayName(),          130),
                UiUtil.column("Số ngày",    c -> String.valueOf(c.getSoNgayGiao()),                    90));
        UiUtil.setupTable(ungVienTable);

        allocationTable.getColumns().addAll(
                UiUtil.column("Mã Site",      PhanBo::getSiteCode,                                   110),
                UiUtil.column("Mã hàng",      PhanBo::getMerchandiseCode,                            150),
                UiUtil.column("Số lượng",     a -> String.valueOf(a.getQuantityOrdered()),            150),
                UiUtil.column("Đơn vị",       PhanBo::getUnit,                                        90),
                UiUtil.column("Phương tiện",  a -> a.getPhuongThucGiaoHang().getCode(),              160));
        UiUtil.setupTable(allocationTable);

        requestBox.setOnAction(event -> loadItems());
        preview.setOnAction(event -> previewUngViens());
        create.setOnAction(event -> createPlan());

        page.getChildren().addAll(title, toolbar,
                section("Danh sách Site có thể đáp ứng", ungVienTable),
                section("Thông tin đặt hàng gửi tới Site", allocationTable));
        loadRequests();
        return page;
    }

    private VBox section(String title, TableView<?> table) {
        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold;");
        VBox box = new VBox(8, label, table);
        box.getStyleClass().add("section");
        return box;
    }

    private void loadRequests() {
        List<YeuCauNhapHang> requests = context.getYeuCauNhapHangController().findAll(null).stream()
                .filter(r -> r.getStatus() == TrangThaiYeuCau.SENT)
                .collect(Collectors.toList());
        requestBox.setItems(FXCollections.observableArrayList(requests));
        if (!requests.isEmpty()) {
            requestBox.getSelectionModel().selectFirst();
            loadItems();
        }
    }

    private void loadItems() {
        YeuCauNhapHang ycnh = requestBox.getSelectionModel().getSelectedItem();
        itemBox.getItems().clear();
        ungVienTable.getItems().clear();
        allocationTable.getItems().clear();
        if (ycnh != null) {
            itemBox.setItems(FXCollections.observableArrayList(ycnh.getItems()));
            if (!ycnh.getItems().isEmpty()) itemBox.getSelectionModel().selectFirst();
        }
    }

    private void previewUngViens() {
        ChiTietHangHoa item = itemBox.getSelectionModel().getSelectedItem();
        if (item == null) { UiUtil.error("Vui lòng chọn mặt hàng cần lập phương án."); return; }
        ungVienTable.setItems(FXCollections.observableArrayList(
                context.getPhuongAnController().previewUngViens(item)));
    }

    private void createPlan() {
        YeuCauNhapHang ycnh = requestBox.getSelectionModel().getSelectedItem();
        ChiTietHangHoa item = itemBox.getSelectionModel().getSelectedItem();
        if (ycnh == null || item == null) { UiUtil.error("Vui lòng chọn yêu cầu và mặt hàng."); return; }
        try {
            if (!UiUtil.confirm("Xác nhận phương án", "Lưu phương án nhập hàng cho " + item.getMerchandiseCode() + "?")) return;
            PhuongAnNhapHang phuongAn = context.getPhuongAnController().createAutomaticPlan(ycnh, item);
            allocationTable.setItems(FXCollections.observableArrayList(phuongAn.getAllocations()));
            UiUtil.info("Lưu thành công", "Phương án " + phuongAn.getPlanCode() + " đã được tạo.");
        } catch (ValidationException ex) {
            UiUtil.error(ex.getMessage());
        }
    }
}
