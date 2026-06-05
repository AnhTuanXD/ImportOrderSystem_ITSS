package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.ChiTietHangHoa;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.TrangThaiYeuCau;
import com.itss.importorder.entity.VaiTro;
import com.itss.importorder.entity.YeuCauNhapHang;
import com.itss.importorder.util.UiUtil;
import com.itss.importorder.util.ValidationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class YeuCauNhapHangBoundary {
    private final AppContext context;
    private final NguoiDung nguoiDung;
    private final TableView<YeuCauNhapHang> table = new TableView<>();
    private final HBox statsBar = new HBox(12);

    public YeuCauNhapHangBoundary(AppContext context, NguoiDung nguoiDung) {
        this.context = context;
        this.nguoiDung = nguoiDung;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Quản lý yêu cầu nhập hàng");
        title.getStyleClass().add("page-title");

        TextField search    = new TextField();
        search.setPromptText("Tìm theo mã yêu cầu, trạng thái, mã hàng");
        boolean isOverseas = nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER;

        Button searchButton = UiUtil.primaryButton("Tìm kiếm");
        Button showAll      = new Button("Xem tất cả");
        Button create       = UiUtil.primaryButton("Tạo yêu cầu");
        HBox toolbar = new HBox(10, search, searchButton, showAll);
        if (!isOverseas) toolbar.getChildren().add(create);
        toolbar.getStyleClass().add("toolbar");

        table.getColumns().addAll(
                UiUtil.column("Mã yêu cầu", YeuCauNhapHang::getRequestCode,                       140),
                UiUtil.column("Mặt hàng",   this::getMerchandiseNames,                             200),
                UiUtil.column("Ngày tạo",   r -> r.getCreatedDate().toString(),                    120),
                UiUtil.column("Người tạo",  YeuCauNhapHang::getCreatedBy,                          120),
                UiUtil.column("Trạng thái", r -> r.getStatus().getDisplayName(),                   160),
                UiUtil.column("Tổng SL",    r -> String.valueOf(r.getTotalQuantity()),               90));
        UiUtil.setupTable(table);
        table.setPlaceholder(new Label("Không tìm thấy yêu cầu nhập hàng nào."));

        Button detail = new Button("Xem chi tiết");
        Button edit   = new Button("Chỉnh sửa");
        Button delete = UiUtil.dangerButton("Xóa yêu cầu");
        HBox actions  = new HBox(10, detail);
        if (!isOverseas) actions.getChildren().addAll(edit, delete);

        searchButton.setOnAction(event -> refresh(context.getYeuCauNhapHangController().search(search.getText(), ownerFilter())));
        showAll.setOnAction(event -> refresh(context.getYeuCauNhapHangController().findAll(ownerFilter())));
        create.setOnAction(event -> showRequestDialog(null));
        detail.setOnAction(event -> showDetail(table.getSelectionModel().getSelectedItem()));
        edit.setOnAction(event -> showRequestDialog(table.getSelectionModel().getSelectedItem()));
        delete.setOnAction(event -> deleteSelected());

        page.getChildren().addAll(title, statsBar, toolbar, table, actions);
        refresh(context.getYeuCauNhapHangController().findAll(ownerFilter()));
        return page;
    }

    private void refresh(List<YeuCauNhapHang> requests) {
        table.setItems(FXCollections.observableArrayList(requests));
        updateStats(requests);
    }

    private void updateStats(List<YeuCauNhapHang> requests) {
        int total    = requests.size();
        long pending  = requests.stream()
                .filter(r -> r.getStatus() == TrangThaiYeuCau.SENT)
                .count();
        long approved = requests.stream()
                .filter(r -> r.getStatus() == TrangThaiYeuCau.ORDERED || r.getStatus() == TrangThaiYeuCau.RECEIVED)
                .count();
        int approvalRate = total == 0 ? 0 : (int) Math.round(approved * 100.0 / total);

        LocalDate now = LocalDate.now();
        long thisMonthTotal = requests.stream()
                .filter(r -> r.getCreatedDate().getYear() == now.getYear()
                        && r.getCreatedDate().getMonthValue() == now.getMonthValue())
                .count();
        long lastMonthTotal = requests.stream()
                .filter(r -> {
                    LocalDate lm = now.minusMonths(1);
                    return r.getCreatedDate().getYear() == lm.getYear()
                            && r.getCreatedDate().getMonthValue() == lm.getMonthValue();
                }).count();
        String totalTrend = lastMonthTotal == 0 ? "" :
                String.format("%+.0f%% tháng này", (thisMonthTotal - lastMonthTotal) * 100.0 / lastMonthTotal);

        long thisMonthApproved = requests.stream()
                .filter(r -> (r.getStatus() == TrangThaiYeuCau.ORDERED || r.getStatus() == TrangThaiYeuCau.RECEIVED)
                        && r.getCreatedDate().getYear() == now.getYear()
                        && r.getCreatedDate().getMonthValue() == now.getMonthValue())
                .count();
        long lastMonthApproved = requests.stream()
                .filter(r -> {
                    LocalDate lm = now.minusMonths(1);
                    return (r.getStatus() == TrangThaiYeuCau.ORDERED || r.getStatus() == TrangThaiYeuCau.RECEIVED)
                            && r.getCreatedDate().getYear() == lm.getYear()
                            && r.getCreatedDate().getMonthValue() == lm.getMonthValue();
                }).count();
        long lastMonthTotalForRate = lastMonthTotal;
        int lastMonthRate = lastMonthTotalForRate == 0 ? 0 :
                (int) Math.round(lastMonthApproved * 100.0 / lastMonthTotalForRate);
        String rateTrend = lastMonthTotalForRate == 0 ? "" :
                String.format("%+d%% so với tháng trước", approvalRate - lastMonthRate);

        statsBar.getChildren().setAll(
                buildStatCard("Tổng yêu cầu", String.valueOf(total),    totalTrend, "#1976D2"),
                buildStatCard("Chờ xử lý",    String.valueOf(pending),  "",         "#FF9800"),
                buildStatCard("Đã duyệt",      String.valueOf(approved), "",         "#4CAF50"),
                buildStatCard("Tỷ lệ duyệt",   approvalRate + "%",      rateTrend,  "#7B1FA2"));
    }

    private VBox buildStatCard(String label, String value, String trend, String color) {
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
        if (!trend.isEmpty()) {
            Label trendLabel = new Label(trend);
            trendLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 11;");
            card.getChildren().add(trendLabel);
        }
        return card;
    }

    private void showDetail(YeuCauNhapHang ycnh) {
        if (ycnh == null) { UiUtil.error("Vui lòng chọn một yêu cầu."); return; }

        VBox detailBox = new VBox(15);
        detailBox.setPadding(new Insets(15));
        Label status = new Label("Trạng thái: " + ycnh.getStatus().getDisplayName());
        status.setStyle("-fx-font-weight: bold; -fx-text-fill: #0066cc;");

        VBox itemsBox = new VBox(10);
        for (ChiTietHangHoa item : ycnh.getItems()) {
            VBox itemDetail = new VBox(8);
            itemDetail.setPadding(new Insets(12));
            itemDetail.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-color: #f9f9f9;");
            boolean salesView = nguoiDung.getVaiTro() == VaiTro.SALES;
            itemDetail.getChildren().addAll(
                    createDetailRow("Mã hàng",         item.getMerchandiseCode()),
                    createDetailRow("Tên mặt hàng",    item.getMerchandiseName()),
                    createDetailRow("Danh mục",         item.getCategory()),
                    new javafx.scene.control.Separator(),
                    createDetailRow("Số lượng",         String.valueOf(item.getQuantityOrdered())),
                    createDetailRow("Đơn vị",           item.getUnit()),
                    new javafx.scene.control.Separator(),
                    createDetailRow("Ngày yêu cầu",     item.getRequestDate().toString()),
                    createDetailRow("Ngày cần hàng",    item.getDesiredDeliveryDate().toString()),
                    new javafx.scene.control.Separator(),
                    createDetailRow("Ghi chú",          item.getNotes()));
            if (!salesView) {
                itemDetail.getChildren().addAll(
                    new javafx.scene.control.Separator(),
                    createDetailRow("Nhà cung cấp",       item.getSupplier()),
                    createDetailRow("Giá ước tính (USD)", String.format("%.2f", item.getEstimatedPrice())));
            }
            itemsBox.getChildren().add(itemDetail);
        }

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(itemsBox);
        scroll.setFitToWidth(true);
        detailBox.getChildren().addAll(status, new javafx.scene.control.Separator(), scroll);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết yêu cầu " + ycnh.getRequestCode());
        dialog.getDialogPane().setContent(detailBox);
        dialog.getDialogPane().setPrefWidth(700);
        dialog.getDialogPane().setPrefHeight(600);
        dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private HBox createDetailRow(String label, String value) {
        Label labelNode = new Label(label + ":");
        labelNode.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
        Label valueNode = new Label(value);
        valueNode.setWrapText(true);
        HBox row = new HBox(10, labelNode, valueNode);
        HBox.setHgrow(valueNode, javafx.scene.layout.Priority.ALWAYS);
        return row;
    }

    private void showRequestDialog(YeuCauNhapHang target) {
        boolean isSales = nguoiDung.getVaiTro() == VaiTro.SALES;
        // --- Danh sách mặt hàng (observable để table tự cập nhật) ---
        ObservableList<ChiTietHangHoa> itemsList = FXCollections.observableArrayList();
        if (target != null) itemsList.addAll(target.getItems());

        TableView<ChiTietHangHoa> itemsTable = new TableView<>(itemsList);
        itemsTable.setPrefHeight(150);
        itemsTable.setPlaceholder(new Label("Chưa có mặt hàng. Điền form bên dưới rồi nhấn \"+ Thêm\"."));
        itemsTable.getColumns().addAll(
                UiUtil.column("Mã hàng",     ChiTietHangHoa::getMerchandiseCode,              100),
                UiUtil.column("Tên mặt hàng",ChiTietHangHoa::getMerchandiseName,              200),
                UiUtil.column("Số lượng",    i -> String.valueOf(i.getQuantityOrdered()),       80),
                UiUtil.column("Đơn vị",      ChiTietHangHoa::getUnit,                          80));
        if (!isSales) {
            itemsTable.getColumns().add(
                UiUtil.column("Giá (USD)",   i -> String.format("%.2f", i.getEstimatedPrice()), 90));
        }

        Button removeBtn = UiUtil.dangerButton("Xóa mặt hàng đã chọn");
        removeBtn.setOnAction(e -> {
            ChiTietHangHoa sel = itemsTable.getSelectionModel().getSelectedItem();
            if (sel != null) itemsList.remove(sel);
            else UiUtil.error("Vui lòng chọn mặt hàng cần xóa trong danh sách.");
        });

        // --- Form nhập mặt hàng mới ---

        TextField code       = new TextField();
        TextField name       = new TextField();
        TextField category   = new TextField("Electronics");
        TextField quantity   = new TextField("1");
        TextField unit       = new TextField("PCS (Cái)");
        DatePicker requestDate = new DatePicker(LocalDate.now());
        DatePicker desiredDate = new DatePicker(LocalDate.now().plusDays(10));
        TextField supplier   = new TextField("Nhà cung cấp mặc định");
        TextField price      = new TextField("0.00");
        javafx.scene.control.TextArea notes = new javafx.scene.control.TextArea();
        notes.setWrapText(true);
        notes.setPrefRowCount(2);

        Button addItemBtn = UiUtil.primaryButton("+ Thêm vào danh sách");
        addItemBtn.setOnAction(e -> {
            try {
                if (code.getText().isBlank()) { UiUtil.error("Mã hàng không được để trống."); return; }
                if (name.getText().isBlank()) { UiUtil.error("Tên mặt hàng không được để trống."); return; }
                int qty = Integer.parseInt(quantity.getText().trim());
                if (qty <= 0) { UiUtil.error("Số lượng phải lớn hơn 0."); return; }
                if (unit.getText().isBlank()) { UiUtil.error("Đơn vị không được để trống."); return; }
                double estimatedPrice = isSales ? 0.0 : Double.parseDouble(price.getText().trim());
                itemsList.add(new ChiTietHangHoa(
                        code.getText().trim(), name.getText().trim(), category.getText().trim(),
                        qty, unit.getText().trim(),
                        0,
                        requestDate.getValue(), desiredDate.getValue(),
                        isSales ? "" : supplier.getText().trim(),
                        estimatedPrice,
                        notes.getText().trim()));
                code.clear(); name.clear(); quantity.setText("1"); notes.clear();
                code.requestFocus();
            } catch (NumberFormatException ex) {
                UiUtil.error("Số lượng hoặc Giá không đúng định dạng số.");
            }
        });

        VBox itemForm = new VBox(12);
        itemForm.setPadding(new Insets(10, 10, 4, 10));
        VBox basicSection = createSection("Thông tin cơ bản",
                createRow("Mã hàng *",       code),
                createRow("Tên mặt hàng *",  name),
                createRow("Danh mục",         category));
        VBox qtySection = createSection("Số lượng & Đơn vị",
                createRow("Số lượng *",      quantity),
                createRow("Đơn vị *",         unit));
        VBox timeSection = createSection("Thời gian",
                createRow("Ngày yêu cầu *",  requestDate),
                createRow("Ngày cần hàng *", desiredDate));
        VBox noteSection = createSection("Ghi chú", new Label("Ghi chú"), notes);

        itemForm.getChildren().addAll(basicSection, qtySection, timeSection);
        if (!isSales) {
            itemForm.getChildren().add(createSection("Nhà cung cấp & giá",
                    createRow("Tên nhà cung cấp",    supplier),
                    createRow("Giá ước tính (USD)",  price)));
        }
        itemForm.getChildren().addAll(noteSection, addItemBtn);

        javafx.scene.control.ScrollPane formScroll = new javafx.scene.control.ScrollPane(itemForm);
        formScroll.setFitToWidth(true);
        formScroll.setPrefHeight(380);

        // --- Layout tổng thể của dialog ---
        Label listLabel = new Label("Danh sách mặt hàng trong yêu cầu");
        listLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        Label formLabel = new Label("Nhập thông tin để thêm mặt hàng");
        formLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(12));
        dialogContent.getChildren().addAll(
                listLabel, itemsTable, removeBtn,
                new javafx.scene.control.Separator(),
                formLabel, formScroll);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(target == null ? "Tạo yêu cầu nhập hàng" : "Chỉnh sửa yêu cầu nhập hàng");
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(dialogContent);
        pane.setPrefWidth(700);
        pane.setPrefHeight(720);
        pane.getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == javafx.scene.control.ButtonType.OK);

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            if (itemsList.isEmpty()) {
                UiUtil.error("Yêu cầu phải có ít nhất một mặt hàng.");
                return;
            }
            String action = target == null ? "tạo yêu cầu nhập hàng mới"
                    : "chỉnh sửa yêu cầu " + target.getRequestCode();
            if (!UiUtil.confirm("Xác nhận", "Bạn có chắc chắn muốn " + action + "?")) return;
            try {
                if (target == null) {
                    context.getYeuCauNhapHangController().create(nguoiDung.getUsername(), new ArrayList<>(itemsList));
                } else {
                    context.getYeuCauNhapHangController().updateItems(target, new ArrayList<>(itemsList));
                }
                refresh(context.getYeuCauNhapHangController().findAll(ownerFilter()));
                UiUtil.info("Thành công", "Dữ liệu yêu cầu nhập hàng đã được lưu.");
            } catch (ValidationException ex) {
                UiUtil.error(ex.getMessage());
            } catch (Exception ex) {
                UiUtil.error("Lỗi không xác định: " + ex.getMessage());
            }
        });
    }

    private VBox createSection(String title, javafx.scene.Node... children) {
        Label sectionLabel = new Label(title);
        sectionLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #333;");
        VBox section = new VBox(10);
        section.getChildren().add(sectionLabel);
        for (javafx.scene.Node child : children) section.getChildren().add(child);
        return section;
    }

    private HBox createRow(String label, javafx.scene.Node control) {
        Label labelNode = new Label(label);
        labelNode.setPrefWidth(150);
        HBox row = new HBox(10, labelNode, control);
        HBox.setHgrow(control, javafx.scene.layout.Priority.ALWAYS);
        return row;
    }

    private String getMerchandiseNames(YeuCauNhapHang ycnh) {
        return ycnh.getItems().stream()
                .map(ChiTietHangHoa::getMerchandiseName)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private void deleteSelected() {
        YeuCauNhapHang ycnh = table.getSelectionModel().getSelectedItem();
        if (ycnh == null) { UiUtil.error("Vui lòng chọn một yêu cầu."); return; }
        if (UiUtil.confirm("Xác nhận xóa", "Bạn chắc chắn muốn xóa " + ycnh.getRequestCode() + "?")) {
            context.getYeuCauNhapHangController().delete(ycnh);
            refresh(context.getYeuCauNhapHangController().findAll(ownerFilter()));
            UiUtil.info("Xóa thành công", "Yêu cầu nhập hàng đã được xóa khỏi danh sách.");
        }
    }

    /** SALES chỉ thấy yêu cầu của chính mình; các role khác thấy tất cả. */
    private String ownerFilter() {
        return nguoiDung.getVaiTro() == VaiTro.SALES ? nguoiDung.getUsername() : null;
    }
}
