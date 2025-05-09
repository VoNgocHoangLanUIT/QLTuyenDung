package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.KQBaiTest;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.repository.DonUngTuyenRepository;
import com.example.QLTuyenDung.repository.KQBaiTestRepository;
import com.example.QLTuyenDung.repository.PhongVanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KQBaiTestService {
    private final KQBaiTestRepository kqBaiTestRepository;
    private final DonUngTuyenRepository donUngTuyenRepository;
    private final PhongVanRepository phongVanRepository;

    public List<KQBaiTest> getAllKQBaiTest() {
        return kqBaiTestRepository.findAll();
    }

    public List<DonUngTuyen> filterKetQuaBaiTest(
            Integer diemNgonNgu, 
            Integer diemLogic, 
            Integer diemChuyenMon) {
        
        List<DonUngTuyen> allDons = donUngTuyenRepository.findAll();
        
        return allDons.stream()
            .filter(don -> isMatchFilter(don, "ngonngu", diemNgonNgu))
            .filter(don -> isMatchFilter(don, "logic", diemLogic))
            .filter(don -> isMatchFilter(don, "chuyenmon", diemChuyenMon))
            .collect(Collectors.toList());
    }

    private boolean isMatchFilter(DonUngTuyen don, String loaiTest, Integer diemMin) {
        if (diemMin == null) {
            return true; // Skip filter if not specified
        }

        return don.getDSKQBaiTest().stream()
            .filter(kq -> kq.getBaiTest().getLoai().equals(loaiTest))
            .mapToInt(KQBaiTest::getDiem)
            .allMatch(diem -> diem >= diemMin);
    }

    public KQBaiTest addKQBaiTest(KQBaiTest kqBaiTest) {
        if (kqBaiTest.getDonUngTuyen() == null || kqBaiTest.getDonUngTuyen().getId() == null) {
            throw new RuntimeException("Vui lòng chọn đơn ứng tuyển!");
        }

        if (kqBaiTest.getBaiTest() == null || kqBaiTest.getBaiTest().getId() == null) {
            throw new RuntimeException("Vui lòng chọn bài test!");
        }

        if (kqBaiTest.getDiem() < 0 || kqBaiTest.getDiem() > 100) {
            throw new RuntimeException("Điểm phải từ 0 đến 100!");
        }

        if (kqBaiTest.getNgayLam() == null) {
            throw new RuntimeException("Vui lòng chọn ngày làm bài!");
        }

        boolean exists = kqBaiTestRepository.existsByDonUngTuyenIdAndBaiTestId(
            kqBaiTest.getDonUngTuyen().getId(),
            kqBaiTest.getBaiTest().getId()
        );
        if (exists) {
            throw new RuntimeException("Đã tồn tại kết quả bài test này cho ứng viên!");
        }

        return kqBaiTestRepository.save(kqBaiTest);
    }

    public KQBaiTest getKQBaiTestById(Long id) {
        return kqBaiTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả bài test!"));
    }

    public KQBaiTest updateKQBaiTest(KQBaiTest kqBaiTest) {
        if (!kqBaiTestRepository.existsById(kqBaiTest.getId())) {
            throw new RuntimeException("Không tìm thấy kết quả bài test!");
        }
        
        if (kqBaiTest.getDiem() < 0 || kqBaiTest.getDiem() > 100) {
            throw new RuntimeException("Điểm phải từ 0 đến 100!");
        }
        return kqBaiTestRepository.save(kqBaiTest);
    }

    public void deleteKQBaiTest(Long id) {
        if (!kqBaiTestRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy kết quả bài test!");
        }
        kqBaiTestRepository.deleteById(id);
    }

    @Transactional
    public void duyetPhongVan(List<Long> donIds) {
        if (donIds == null || donIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một ứng viên nếu bạn muốn duyệt phỏng vấn!");
        }

        // Get all applications
        List<DonUngTuyen> allDons = donUngTuyenRepository.findAll();
        
        // Separate into qualified and unqualified
        List<DonUngTuyen> donDuDK = allDons.stream()
            .filter(this::dieuKienPhongVan)
            .collect(Collectors.toList());
        
        List<DonUngTuyen> donKhongDuDK = allDons.stream()
            .filter(don -> !dieuKienPhongVan(don))
            .collect(Collectors.toList());

        // Among qualified, separate selected and unselected
        List<DonUngTuyen> donDaChon = donDuDK.stream()
            .filter(don -> donIds.contains(don.getId()))
            .collect(Collectors.toList());
        
        List<DonUngTuyen> donKhongChon = donDuDK.stream()
            .filter(don -> !donIds.contains(don.getId()))
            .collect(Collectors.toList());

        List<PhongVan> phongVans = new ArrayList<>();
        
        // Process selected applications
        for (DonUngTuyen don : donDaChon) {
            don.setTrangThai("phongvan");
            
            PhongVan phongVan = new PhongVan();
            phongVan.setDonUngTuyen(don);
            phongVan.setTrangThai("chopv");
            phongVans.add(phongVan);
        }

        // Update status for both unselected qualified and unqualified applications
        for (DonUngTuyen don : donKhongChon) {
            don.setTrangThai("tuchoi");
        }
        
        for (DonUngTuyen don : donKhongDuDK) {
            don.setTrangThai("tuchoi");
        }
        
        // Save all changes
        donUngTuyenRepository.saveAll(donDaChon);
        donUngTuyenRepository.saveAll(donKhongChon);
        donUngTuyenRepository.saveAll(donKhongDuDK);
        phongVanRepository.saveAll(phongVans);
    }

    private boolean dieuKienPhongVan(DonUngTuyen don) {
        // Get all required tests for this job posting
        Set<String> allTestCuaTinTD = don.getTinTuyenDung().getDSBaiTest().stream()
            .map(test -> test.getLoai())
            .collect(Collectors.toSet());
        
        // Check if candidate has taken all required tests
        Set<String> allTestDaLam = don.getDSKQBaiTest().stream()
            .map(kq -> kq.getBaiTest().getLoai())
            .collect(Collectors.toSet());
        
        // Only check if all required tests are completed
        return allTestDaLam.containsAll(allTestCuaTinTD);
    }
}
