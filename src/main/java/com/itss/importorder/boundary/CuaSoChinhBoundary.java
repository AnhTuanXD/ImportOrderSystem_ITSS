package com.itss.importorder.boundary;

import com.itss.importorder.AppContext;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.VaiTro;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class CuaSoChinhBoundary {
    private final AppContext context;
    private final NguoiDung nguoiDung;
    private final Runnable onLogout;
    private final BorderPane root = new BorderPane();

    public CuaSoChinhBoundary(AppContext context, NguoiDung nguoiDung, Runnable onLogout) {
        this.context = context;
        this.nguoiDung = nguoiDung;
        this.onLogout = onLogout;
    }

    public Parent build() {
        root.setLeft(buildSidebar());
        showHome();
        return root;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPrefWidth(250);
        sidebar.getStyleClass().add("sidebar");

        Label title = new Label("Import Order");
        title.getStyleClass().add("sidebar-title");
        Label role = new Label(nguoiDung.getVaiTro().getDisplayName());
        role.getStyleClass().add("sidebar-subtitle");

        Button home      = navButton("Trang chủ",           this::showHome);
        Button requests  = navButton("Yêu cầu nhập hàng",  () -> root.setCenter(new YeuCauNhapHangBoundary(context, nguoiDung).build()));
        Button sites     = navButton("Quản lý Site",        () -> root.setCenter(new DiaDiemNhapBoundary(context).build()));
        Button siteStock = navButton("Quản lý mặt hàng",   () -> root.setCenter(new TonKhoBoundary(context, nguoiDung).build()));
        Button xacNhan   = navButton("Xác nhận đơn hàng",  () -> root.setCenter(new XacNhanDonHangBoundary(context, nguoiDung).build()));
        Button planning  = navButton("Lập phương án",       () -> root.setCenter(new PhuongAnNhapHangBoundary(context).build()));
        Button warehouse = navButton("Quản lý kho",         () -> root.setCenter(new KiemHangBoundary(context).build()));
        Button admin     = navButton("Quản trị tài khoản", () -> root.setCenter(new QuanTriTaiKhoanBoundary(context, nguoiDung).build()));
        Button logout    = navButton("Đăng xuất",           onLogout);

        sidebar.getChildren().addAll(title, role, home);
        if (nguoiDung.getVaiTro() == VaiTro.ADMIN) {
            sidebar.getChildren().add(admin);
        }
        if (nguoiDung.getVaiTro() == VaiTro.SALES || nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER) {
            sidebar.getChildren().add(requests);
        }
        if (nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER) {
            sidebar.getChildren().add(sites);
        }
        if (nguoiDung.getVaiTro() == VaiTro.IMPORT_SITE) {
            sidebar.getChildren().addAll(xacNhan, siteStock);
        }
        if (nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER) {
            sidebar.getChildren().add(planning);
        }
        if (nguoiDung.getVaiTro() == VaiTro.WAREHOUSE || nguoiDung.getVaiTro() == VaiTro.OVERSEAS_ORDER) {
            sidebar.getChildren().add(warehouse);
        }
        sidebar.getChildren().add(logout);
        return sidebar;
    }

    private Button navButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> action.run());
        return button;
    }

    private void showHome() {
        VBox page = new VBox(14);
        page.getStyleClass().add("page");
        page.setPadding(new Insets(18));
        Label title = new Label("Trang chủ");
        title.getStyleClass().add("page-title");
        Label summary = new Label("Chọn chức năng ở thanh bên để xử lý quy trình đặt hàng nhập khẩu.");
        Label flow = new Label("Luồng chính: Bán hàng tạo yêu cầu -> Đặt hàng quốc tế lập phương án -> Kho kiểm hàng và báo cáo.");
        page.getChildren().addAll(title, summary, flow);
        root.setCenter(page);
    }
}
