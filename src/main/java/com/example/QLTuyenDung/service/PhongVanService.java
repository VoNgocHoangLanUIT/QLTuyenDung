package com.example.QLTuyenDung.service;

import java.util.ArrayList;
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
}
