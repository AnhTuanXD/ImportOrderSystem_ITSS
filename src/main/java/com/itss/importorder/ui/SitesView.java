package com.itss.importorder.ui;

import com.itss.importorder.AppContext;
import com.itss.importorder.model.ImportSite;
import com.itss.importorder.model.SiteStatus;
import com.itss.importorder.service.ValidationException;
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

public class SitesView {
    private final AppContext context;
    private final TableView<ImportSite> table = new TableView<>();

    public SitesView(AppContext context) {
        this.context = context;
    }

    public Parent build() {
        VBox page = new VBox(12);
        page.getStyleClass().add("page");

        Label title = new Label("Quản lý Site nhập khẩu");
        title.getStyleClass().add("page-title");

        Button add = UiUtil.primaryButton("Thêm Site");
        Button detail = new Button("Xem / chỉnh sửa");
        Button activate = new Button("Kích hoạt");
        Button disable = UiUtil.dangerButton("Vô hiệu hóa");
        HBox toolbar = new HBox(10, add, detail, activate, disable);
        toolbar.getStyleClass().add("toolbar");

        table.getColumns().addAll(
                UiUtil.column("Mã Site", ImportSite::getSiteCode, 100),
                UiUtil.column("Tên Site", ImportSite::getName, 180),
                UiUtil.column("Tàu", site -> site.getDeliveryDaysByShip() + " ngày", 90),
                UiUtil.column("Hàng không", site -> site.getDeliveryDaysByAir() + " ngày", 110),
                UiUtil.column("Trạng thái", site -> site.getStatus().getDisplayName(), 140),
                UiUtil.column("Thông tin khác", ImportSite::getOtherInformation, 220));
        UiUtil.setupTable(table);

        add.setOnAction(event -> showSiteDialog(null));
        detail.setOnAction(event -> {
            ImportSite site = selected();
            if (site != null) {
                showSiteDialog(site);
            }
        });
        activate.setOnAction(event -> changeStatus(SiteStatus.ACTIVE));
        disable.setOnAction(event -> changeStatus(SiteStatus.DISABLED));

        page.getChildren().addAll(title, toolbar, table);
        refresh();
        return page;
    }

    private void refresh() {
        List<ImportSite> sites = context.getSiteService().findAll();
        table.setItems(FXCollections.observableArrayList(sites));
    }

    private ImportSite selected() {
        ImportSite site = table.getSelectionModel().getSelectedItem();
        if (site == null) {
            UiUtil.error("Vui lòng chọn một Site.");
        }
        return site;
    }

    private void showSiteDialog(ImportSite site) {
        TextField code = new TextField(site == null ? "" : site.getSiteCode());
        code.setDisable(site != null);
        TextField name = new TextField(site == null ? "" : site.getName());
        TextField password = new TextField(site == null ? "123456" : site.getPassword());
        TextField ship = new TextField(site == null ? "12" : String.valueOf(site.getDeliveryDaysByShip()));
        TextField air = new TextField(site == null ? "4" : String.valueOf(site.getDeliveryDaysByAir()));
        TextArea info = new TextArea(site == null ? "" : site.getOtherInformation());
        info.setPrefRowCount(3);

        GridPane form = new GridPane();
        form.setPadding(new Insets(12));
        form.setVgap(10);
        form.setHgap(10);
        form.addRow(0, new Label("Mã Site"), code);
        form.addRow(1, new Label("Tên Site"), name);
        form.addRow(2, new Label("Mật khẩu"), password);
        form.addRow(3, new Label("Số ngày bằng tàu"), ship);
        form.addRow(4, new Label("Số ngày bằng máy bay"), air);
        form.addRow(5, new Label("Thông tin khác"), info);

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle(site == null ? "Thêm Site" : "Chi tiết Site");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button -> button == ButtonType.OK);

        dialog.showAndWait().filter(Boolean::booleanValue).ifPresent(ignored -> {
            try {
                String action = site == null ? "thêm Site" : "cập nhật Site";
                if (!UiUtil.confirm("Xác nhận " + action, "Bạn chắc chắn muốn " + action + "?")) {
                    return;
                }
                int shipDays = Integer.parseInt(ship.getText().trim());
                int airDays = Integer.parseInt(air.getText().trim());
                if (site == null) {
                    context.getSiteService().add(code.getText().trim(), name.getText().trim(),
                            password.getText(), shipDays, airDays, info.getText());
                } else {
                    context.getSiteService().update(site, name.getText().trim(), password.getText(),
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

    private void changeStatus(SiteStatus status) {
        ImportSite site = selected();
        if (site == null) {
            return;
        }
        if (UiUtil.confirm("Xác nhận thay đổi trạng thái",
                "Bạn chắc chắn muốn chuyển " + site.getSiteCode() + " sang " + status.getDisplayName() + "?")) {
            context.getSiteService().setStatus(site, status);
            refresh();
            UiUtil.info("Thành công", "Trạng thái Site đã được cập nhật.");
        }
    }
}


