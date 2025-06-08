package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QLTuyenDung.model.BaiTest;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.repository.DonUngTuyenRepository;
import com.example.QLTuyenDung.repository.PhongVanRepository;
import com.example.QLTuyenDung.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhongVanService {
    private final PhongVanRepository phongVanRepository;
    private final UserRepository userRepository;
    private final DonUngTuyenRepository donUngTuyenRepository;  

    public List<PhongVan> getAllPhongVan() {
        return phongVanRepository.findAll();
    }

    @Transactional
    public void phanCongPhongVan(Long phongVanId, List<Long> nhanVienIds) {
        // Lấy phỏng vấn gốc để lấy thông tin đơn ứng tuyển
        PhongVan phongVanGoc = phongVanRepository.findById(phongVanId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phỏng vấn với ID: " + phongVanId));

        List<PhongVan> danhSachPhongVan = new ArrayList<>();

        // Tạo phỏng vấn mới cho từng nhân viên
        for (Long nhanVienId : nhanVienIds) {
            User nhanVien = userRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + nhanVienId));

            // Kiểm tra role của nhân viên
            boolean isHRStaff = nhanVien.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName().equals("HR_STAFF"));

            if (!isHRStaff) {
                throw new RuntimeException("Nhân viên " + nhanVien.getHoTen() + " không có quyền phỏng vấn");
            }

            // Tạo phỏng vấn mới
            PhongVan phongVanMoi = new PhongVan();
            phongVanMoi.setDonUngTuyen(phongVanGoc.getDonUngTuyen());
            phongVanMoi.setTrangThai("chopv");
            phongVanMoi.setNhanVienTD(nhanVien);
            
            danhSachPhongVan.add(phongVanMoi);
        }

        // Lưu tất cả phỏng vấn mới
        phongVanRepository.saveAll(danhSachPhongVan);
    }

    public PhongVan getPhongVanById(Long id) {
        return phongVanRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phỏng vấn với ID: " + id));
    }

    public void deleteNVTDCuaPhongVan(Long id) {
        PhongVan phongVan = phongVanRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phỏng vấn với ID: " + id));
        phongVanRepository.delete(phongVan);
    }

    public List<User> getNhanVienByDonUngTuyenId(Long donUngTuyenId) {
        DonUngTuyen donUngTuyen = donUngTuyenRepository.findById(donUngTuyenId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển!"));
        List<PhongVan> dSPhongVan = phongVanRepository.findByDonUngTuyen(donUngTuyen);
        return dSPhongVan.stream()
            .map(PhongVan::getNhanVienTD)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }

    public List<PhongVan> getPhongVanByDonUngTuyen(DonUngTuyen donUngTuyen) {
        return phongVanRepository.findByDonUngTuyen(donUngTuyen);
    }

    public PhongVan save(PhongVan phongVan) {
        return phongVanRepository.save(phongVan);
    }

    @Transactional
    public PhongVan checkAndCreatePhongVan(DonUngTuyen donUngTuyen) {
        // Kiểm tra xem đã tồn tại phỏng vấn nào cho đơn ứng tuyển này chưa
        List<PhongVan> existingInterviews = phongVanRepository.findByDonUngTuyen(donUngTuyen);
        
        // Nếu không tìm thấy phỏng vấn nào, tạo phỏng vấn mới
        if (existingInterviews.isEmpty()) {
            PhongVan phongVan = new PhongVan();
            phongVan.setDonUngTuyen(donUngTuyen);
            phongVan.setTrangThai("chopv"); // Trạng thái chờ phỏng vấn
            
            // Tạo ngày phỏng vấn mặc định là 7 ngày sau ngày hiện tại
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 7);
            phongVan.setNgayPV(calendar.getTime());
            
            // Lưu và trả về đối tượng phỏng vấn mới
            return phongVanRepository.save(phongVan);
        }
        
        // Nếu đã có phỏng vấn, trả về null
        return null;
    }

    @Transactional
    public List<PhongVan> nhaTDPhanCongPhongVan(Long donUngTuyenId, List<Long> nhanVienIds, 
                                            Date ngayPV, String diaDiem) {
        // Lấy đơn ứng tuyển
        DonUngTuyen donUngTuyen = donUngTuyenRepository.findById(donUngTuyenId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển!"));
        
        List<PhongVan> danhSachPhongVan = new ArrayList<>();

        // Tạo phỏng vấn cho từng nhân viên được chọn
        for (Long nhanVienId : nhanVienIds) {
            User nhanVien = userRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + nhanVienId));

            // Kiểm tra quyền của nhân viên
            if (!nhanVien.getCongTy().getId().equals(donUngTuyen.getTinTuyenDung().getCongty().getId())) {
                throw new RuntimeException("Nhân viên không thuộc công ty của bạn!");
            }

            // Tạo phỏng vấn mới
            PhongVan phongVan = new PhongVan();
            phongVan.setDonUngTuyen(donUngTuyen);
            phongVan.setNgayPV(ngayPV);
            phongVan.setDiaDiem(diaDiem);
            phongVan.setTrangThai("chopv");
            phongVan.setNhanVienTD(nhanVien);
            
            // Lưu vào danh sách
            danhSachPhongVan.add(phongVanRepository.save(phongVan));
        }

        return danhSachPhongVan;
    }

    @Transactional
    public PhongVan capNhatPhongVan(Long phongVanId, Date ngayPV, String diaDiem, 
                                String trangThai, Long nhanVienId, Integer diemDanhGia, String nhanXet) {
        // Lấy phỏng vấn từ database
        PhongVan phongVan = phongVanRepository.findById(phongVanId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phỏng vấn với ID: " + phongVanId));
        
        // Cập nhật các trường có thay đổi
        if (ngayPV != null) phongVan.setNgayPV(ngayPV);
        if (diaDiem != null) phongVan.setDiaDiem(diaDiem);
        if (trangThai != null) phongVan.setTrangThai(trangThai);
        
        // Cập nhật nhân viên phỏng vấn nếu có thay đổi
        if (nhanVienId != null) {
            User nhanVien = userRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + nhanVienId));
            
            // Kiểm tra nhân viên thuộc công ty phù hợp
            if (!nhanVien.getCongTy().getId().equals(phongVan.getDonUngTuyen().getTinTuyenDung().getCongty().getId())) {
                throw new RuntimeException("Nhân viên không thuộc công ty quản lý đơn ứng tuyển này!");
            }
            
            phongVan.setNhanVienTD(nhanVien);
        }
        
        if (diemDanhGia != null) phongVan.setDiemDanhGia(diemDanhGia);
        if (nhanXet != null) phongVan.setNhanXet(nhanXet);
        // Lưu và trả về phỏng vấn đã cập nhật
        return phongVanRepository.save(phongVan);
    }

    // Phương thức kiểm tra nhân viên đã được phân công cho đơn ứng tuyển cụ thể chưa
    public boolean isNhanVienDaPhanCong(Long nhanVienId, Long donUngTuyenId) {
        DonUngTuyen donUngTuyen = donUngTuyenRepository.findById(donUngTuyenId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn ứng tuyển!"));
        
        List<PhongVan> phongVans = phongVanRepository.findByDonUngTuyen(donUngTuyen);
        
        // Kiểm tra xem nhân viên đã được phân công cho đơn ứng tuyển này chưa
        return phongVans.stream()
            .anyMatch(pv -> pv.getNhanVienTD() != null && pv.getNhanVienTD().getId().equals(nhanVienId));
    }

    @Transactional
    public void deletePhongVan(Long id) {
        PhongVan phongVan = phongVanRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy phỏng vấn với ID: " + id));
        
        // Xóa phỏng vấn
        phongVanRepository.delete(phongVan);
    }

    public List<PhongVan> getPhongVanByNhanVien(User nhanVien) {
        return phongVanRepository.findByNhanVienTD(nhanVien);
    }
}
