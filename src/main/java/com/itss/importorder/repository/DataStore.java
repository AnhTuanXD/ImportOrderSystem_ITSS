package com.itss.importorder.repository;

import com.itss.importorder.entity.BaoCaoKho;
import com.itss.importorder.entity.DiaDiemNhap;
import com.itss.importorder.entity.NguoiDung;
import com.itss.importorder.entity.PhuongAnNhapHang;
import com.itss.importorder.entity.TonKho;
import com.itss.importorder.entity.YeuCauNhapHang;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private final NguoiDungRepository nguoiDungRepository = new NguoiDungRepository();
    private final YeuCauNhapHangRepository ycnhRepository = new YeuCauNhapHangRepository();
    private final DiaDiemNhapRepository diaDiemRepository = new DiaDiemNhapRepository();
    private final TonKhoRepository tonKhoRepository = new TonKhoRepository();
    private final PhuongAnRepository phuongAnRepository = new PhuongAnRepository();
    private final BaoCaoKhoRepository baoCaoKhoRepository = new BaoCaoKhoRepository();

    // ── Getters ──────────────────────────────────────────────────────────────

    public List<NguoiDung> getNguoiDungs() {
        try {
            return nguoiDungRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu người dùng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<YeuCauNhapHang> getYeuCauNhapHangs() {
        try {
            return ycnhRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu yêu cầu nhập hàng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<DiaDiemNhap> getDiaDiemNhaps() {
        try {
            return diaDiemRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu địa điểm nhập: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<TonKho> getTonKhos() {
        try {
            return tonKhoRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu tồn kho: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<PhuongAnNhapHang> getPhuongAnNhapHangs() {
        try {
            return phuongAnRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu phương án nhập hàng: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<BaoCaoKho> getBaoCaoKhos() {
        try {
            return baoCaoKhoRepository.findAll();
        } catch (SQLException e) {
            System.err.println("Lỗi lấy dữ liệu báo cáo kho: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    public void saveNguoiDung(NguoiDung nguoiDung) throws SQLException {
        nguoiDungRepository.save(nguoiDung);
    }

    public void saveYeuCauNhapHang(YeuCauNhapHang ycnh) throws SQLException {
        ycnhRepository.save(ycnh);
    }

    public void saveDiaDiemNhap(DiaDiemNhap diaDiem) throws SQLException {
        diaDiemRepository.save(diaDiem);
    }

    public void saveTonKho(TonKho tonKho) throws SQLException {
        tonKhoRepository.save(tonKho);
    }

    public void savePhuongAnNhapHang(PhuongAnNhapHang phuongAn) throws SQLException {
        phuongAnRepository.save(phuongAn);
    }

    public void confirmAllocation(String planCode, String siteCode, String merchandiseCode) throws SQLException {
        phuongAnRepository.confirmAllocation(planCode, siteCode, merchandiseCode);
    }

    public void saveBaoCaoKho(BaoCaoKho baoCao) throws SQLException {
        baoCaoKhoRepository.save(baoCao);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    public void deleteNguoiDung(String username) throws SQLException {
        nguoiDungRepository.delete(username);
    }

    public void deleteYeuCauNhapHang(String requestCode) throws SQLException {
        ycnhRepository.delete(requestCode);
    }

    public void deleteDiaDiemNhap(String siteCode) throws SQLException {
        diaDiemRepository.delete(siteCode);
    }

    public void deleteTonKho(String siteCode, String merchandiseCode) throws SQLException {
        tonKhoRepository.delete(siteCode, merchandiseCode);
    }

    public void deletePhuongAnNhapHang(String planCode) throws SQLException {
        phuongAnRepository.delete(planCode);
    }

    public void deleteBaoCaoKho(String reportCode) throws SQLException {
        baoCaoKhoRepository.delete(reportCode);
    }

    // ── Find ─────────────────────────────────────────────────────────────────

    public NguoiDung findNguoiDungByUsername(String username) throws SQLException {
        return nguoiDungRepository.findByUsername(username);
    }

    public YeuCauNhapHang findYeuCauNhapHangByCode(String requestCode) throws SQLException {
        return ycnhRepository.findByCode(requestCode);
    }

    public DiaDiemNhap findDiaDiemNhapByCode(String siteCode) throws SQLException {
        return diaDiemRepository.findByCode(siteCode);
    }

    public List<TonKho> findTonKhosBySiteCode(String siteCode) throws SQLException {
        return tonKhoRepository.findBySiteCode(siteCode);
    }

    public PhuongAnNhapHang findPhuongAnByCode(String planCode) throws SQLException {
        return phuongAnRepository.findByCode(planCode);
    }

    public List<PhuongAnNhapHang> findPhuongAnsByRequestCode(String requestCode) throws SQLException {
        return phuongAnRepository.findByRequestCode(requestCode);
    }

    public BaoCaoKho findBaoCaoKhoByCode(String reportCode) throws SQLException {
        return baoCaoKhoRepository.findByCode(reportCode);
    }

    public List<BaoCaoKho> findBaoCaoKhosByRequestCode(String requestCode) throws SQLException {
        return baoCaoKhoRepository.findByRequestCode(requestCode);
    }
}
