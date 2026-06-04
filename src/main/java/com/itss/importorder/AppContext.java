package com.itss.importorder;

import com.itss.importorder.controller.DiaDiemNhapController;
import com.itss.importorder.controller.KiemHangController;
import com.itss.importorder.controller.PhuongAnController;
import com.itss.importorder.controller.QuanTriTaiKhoanController;
import com.itss.importorder.controller.XacThucController;
import com.itss.importorder.controller.YeuCauNhapHangController;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.repository.DataStore;
import com.itss.importorder.repository.SampleDataFactory;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppContext {
    private final DataStore store = SampleDataFactory.create();
    private final XacThucController xacThucController             = new XacThucController(store);
    private final YeuCauNhapHangController ycnhController         = new YeuCauNhapHangController(store);
    private final DiaDiemNhapController diaDiemController         = new DiaDiemNhapController(store);
    private final PhuongAnController phuongAnController            = new PhuongAnController(store);
    private final KiemHangController kiemHangController            = new KiemHangController(store);
    private final QuanTriTaiKhoanController quanTriController      = new QuanTriTaiKhoanController(store);

    public XacThucController getXacThucController() {
        return xacThucController;
    }

    public YeuCauNhapHangController getYeuCauNhapHangController() {
        return ycnhController;
    }

    public DiaDiemNhapController getDiaDiemNhapController() {
        return diaDiemController;
    }

    public PhuongAnController getPhuongAnController() {
        return phuongAnController;
    }

    public KiemHangController getKiemHangController() {
        return kiemHangController;
    }

    public List<TonKho> getTonKhos(String siteCode) {
        try {
            return store.findTonKhosBySiteCode(siteCode);
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu tồn kho: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveTonKho(TonKho tonKho) throws SQLException {
        store.saveTonKho(tonKho);
    }

    public void deleteTonKho(String siteCode, String merchandiseCode) throws SQLException {
        store.deleteTonKho(siteCode, merchandiseCode);
    }

    public QuanTriTaiKhoanController getQuanTriTaiKhoanController() {
        return quanTriController;
    }
}
