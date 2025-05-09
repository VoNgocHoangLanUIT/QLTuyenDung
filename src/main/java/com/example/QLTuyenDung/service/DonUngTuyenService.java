package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Service;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.repository.DonUngTuyenRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DonUngTuyenService {
    
    private final DonUngTuyenRepository donUngTuyenRepository;
    
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

    
}
