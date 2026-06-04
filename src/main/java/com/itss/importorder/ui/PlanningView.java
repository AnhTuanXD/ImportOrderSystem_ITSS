package com.itss.importorder.ui;

import com.itss.importorder.AppContext;
import com.itss.importorder.model.Allocation;
import com.itss.importorder.model.ImportPlan;
import com.itss.importorder.model.ImportRequest;
import com.itss.importorder.model.MerchandiseRequest;
import com.itss.importorder.service.PlanningService;
import com.itss.importorder.service.ValidationException;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class PlanningView {
    private final AppContext context;
    private final ComboBox<ImportRequest> requestBox = new ComboBox<>();
    private final ComboBox<MerchandiseRequest> itemBox = new ComboBox<>();
    private final TableView<PlanningService.CandidatePreview> candidateTable = new TableView<>();
    private final TableView<Allocation> allocationTable = new TableView<>();

    public PlanningView(AppContext context) {
        this.context = context;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Lập phương án nhập hàng từ Sites");
        title.getStyleClass().add("page-title");

        requestBox.setPrefWidth(260);
        requestBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ImportRequest request) {
                return request == null ? "" : request.getRequestCode() + " - " + request.getStatus().getDisplayName();
            }

            @Override
            public ImportRequest fromString(String value) {
                return null;
            }
        });
        itemBox.setPrefWidth(260);
        itemBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(MerchandiseRequest item) {
                return item == null ? "" : item.getMerchandiseCode() + " / " + item.getQuantityOrdered() + " " + item.getUnit();
            }

            @Override
            public MerchandiseRequest fromString(String value) {
                return null;
            }
        });

        Button preview = new Button("Xem Site đáp ứng");
        Button create = UiUtil.primaryButton("Tạo phương án");
        HBox toolbar = new HBox(10, new Label("Yêu cầu"), requestBox, new Label("Mặt hàng"), itemBox, preview, create);
        toolbar.getStyleClass().add("toolbar");

        candidateTable.getColumns().addAll(
                UiUtil.column("Mã Site", PlanningService.CandidatePreview::getSiteCode, 100),
                UiUtil.column("Tên Site", PlanningService.CandidatePreview::getSiteName, 180),
                UiUtil.column("Tên kho", c -> c.getInStockQuantity() + " " + c.getUnit(), 100),
                UiUtil.column("Phương tiện", c -> c.getDeliveryMeans().getDisplayName(), 130),
                UiUtil.column("Số ngày", c -> String.valueOf(c.getDeliveryDays()), 90));
        UiUtil.setupTable(candidateTable);

        allocationTable.getColumns().addAll(
                UiUtil.column("Site code", Allocation::getSiteCode, 110),
                UiUtil.column("Merchandise code", Allocation::getMerchandiseCode, 150),
                UiUtil.column("Quantity ordered", a -> String.valueOf(a.getQuantityOrdered()), 150),
                UiUtil.column("Unit", Allocation::getUnit, 90),
                UiUtil.column("Delivery means", a -> a.getDeliveryMeans().getCode(), 160));
        UiUtil.setupTable(allocationTable);

        requestBox.setOnAction(event -> loadItems());
        preview.setOnAction(event -> previewCandidates());
        create.setOnAction(event -> createPlan());

        page.getChildren().addAll(title, toolbar, section("Danh sách Site có thể đáp ứng", candidateTable),
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
        List<ImportRequest> requests = context.getImportRequestService().findAll();
        requestBox.setItems(FXCollections.observableArrayList(requests));
        if (!requests.isEmpty()) {
            requestBox.getSelectionModel().selectFirst();
            loadItems();
        }
    }

    private void loadItems() {
        ImportRequest request = requestBox.getSelectionModel().getSelectedItem();
        itemBox.getItems().clear();
        candidateTable.getItems().clear();
        allocationTable.getItems().clear();
        if (request != null) {
            itemBox.setItems(FXCollections.observableArrayList(request.getItems()));
            if (!request.getItems().isEmpty()) {
                itemBox.getSelectionModel().selectFirst();
            }
        }
    }

    private void previewCandidates() {
        MerchandiseRequest item = itemBox.getSelectionModel().getSelectedItem();
        if (item == null) {
            UiUtil.error("Vui lòng chọn mặt hàng cần lập phương án.");
            return;
        }
        candidateTable.setItems(FXCollections.observableArrayList(context.getPlanningService().previewCandidates(item)));
    }

    private void createPlan() {
        ImportRequest request = requestBox.getSelectionModel().getSelectedItem();
        MerchandiseRequest item = itemBox.getSelectionModel().getSelectedItem();
        if (request == null || item == null) {
            UiUtil.error("Vui lòng chọn yêu cầu và mặt hàng.");
            return;
        }
        try {
            if (!UiUtil.confirm("Xác nhận phuong án", "Lưu phương án nhập hàng cho " + item.getMerchandiseCode() + "?")) {
                return;
            }
            ImportPlan plan = context.getPlanningService().createAutomaticPlan(request, item);
            allocationTable.setItems(FXCollections.observableArrayList(plan.getAllocations()));
            UiUtil.info("Lưu thành công", "Phương án " + plan.getPlanCode() + " đã được tạo.");
        } catch (ValidationException ex) {
            UiUtil.error(ex.getMessage());
        }
    }
}

