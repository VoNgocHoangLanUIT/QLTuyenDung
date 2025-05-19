package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.repository.DonUngTuyenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DonUngTuyenService {
    
    private final DonUngTuyenRepository donUngTuyenRepository;
    private final PhongVanService phongVanService;
    
    public List<DonUngTuyen> getAllDonUngTuyen() {
        try {
            return donUngTuyenRepository.findAll();
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public DonUngTuyen addDonUngTuyen(DonUngTuyen donUngTuyen) {
        if (donUngTuyen.getUser() == null || donUngTuyen.getUser().getId() == null) {
            throw new RuntimeException("Vui lòng chọn ứng viên!");
        }
        if (donUngTuyen.getTinTuyenDung() == null || donUngTuyen.getTinTuyenDung().getId() == null) {
            throw new RuntimeException("Vui lòng chọn tin tuyển dụng!");
        }

        if (donUngTuyenRepository.existsByUserIdAndTinTuyenDungId(
                donUngTuyen.getUser().getId(), 
                donUngTuyen.getTinTuyenDung().getId())) {
            throw new RuntimeException("Ứng viên đã ứng tuyển vị trí này!");
        }

        donUngTuyen.setTrangThai("dangduyet");
        donUngTuyen.setNgayUngTuyen(new Date());
        return donUngTuyenRepository.save(donUngTuyen);
    }

    public DonUngTuyen getDonUngTuyenById(Long id) {
        return donUngTuyenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển!"));
    }

    public DonUngTuyen updateDonUngTuyen(DonUngTuyen donUngTuyen) {
        if (!donUngTuyenRepository.existsById(donUngTuyen.getId())) {
            throw new RuntimeException("Không tìm thấy đơn ứng tuyển!");
        }
        return donUngTuyenRepository.save(donUngTuyen);
    }

    public void deleteDonUngTuyen(Long id) {
        if (!donUngTuyenRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy đơn ứng tuyển!");
        }
        donUngTuyenRepository.deleteById(id);
    }

    public List<DonUngTuyen> getDonUngTuyenByTinTuyenDungId(Long tinTuyenDungId) {
        try {
            return donUngTuyenRepository.findByTinTuyenDungId(tinTuyenDungId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy đơn ứng tuyển theo tin tuyển dụng: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    // Phương thức hỗ trợ để chuyển đổi mã trạng thái thành thông báo
    public String getStatusMessage(String status) {
        switch (status) {
            case "dangduyet":
                return "Đang duyệt hồ sơ";
            case "chotest":
                return "Chờ bài test";
            case "phongvan":
                return "Phỏng vấn";
            case "datuyen":
                return "Đã tuyển";
            case "tuchoi":
                return "Từ chối";
            default:
                return status;
        }
    }
    public boolean kiemTraTrangThaiPV(DonUngTuyen donUngTuyen) {
        if (donUngTuyen.getTrangThai().equals("phongvan")) {
            // Kiểm tra và tạo phỏng vấn mới nếu cần
            phongVanService.checkAndCreatePhongVan(donUngTuyen);
            return true;
        }
        else {
            return false;
        }
    }

    public List<DonUngTuyen> getDonUngTuyenByTrangThaiVaTinTD(String trangThai, Long tinTDId) {
        return donUngTuyenRepository.findByTrangThaiAndTinTuyenDungId(trangThai, tinTDId);
    }

    public List<DonUngTuyen> getDonUngTuyenByQuyenTestVaTinTD(boolean quyenTest, Long tinTuyenDungId) {
        try {
            return donUngTuyenRepository.findByQuyenTestAndTinTuyenDungId(quyenTest,tinTuyenDungId);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy đơn ứng tuyển theo quyền test và tin tuyển dụng: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
}
