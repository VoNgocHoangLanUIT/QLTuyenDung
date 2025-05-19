package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.BaiTest;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.repository.BaiTestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BaiTestService {
    private final BaiTestRepository baiTestRepository;
    
    public List<BaiTest> getAllBaiTest() {
        return baiTestRepository.findAll();
    }

    public BaiTest addBaiTest(BaiTest baiTest) {
        if (baiTest.getTieuDe() == null || baiTest.getTieuDe().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề không được để trống!");
        }
        if (baiTest.getLoai() == null || baiTest.getLoai().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn loại bài test!");
        }
        if (baiTest.getTinTuyenDung() == null || baiTest.getTinTuyenDung().getId() == null) {
            throw new RuntimeException("Vui lòng chọn tin tuyển dụng!");
        }
        
        return baiTestRepository.save(baiTest);
    }

    public BaiTest getBaiTestById(Long id) {
        return baiTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài test!"));
    }

    public List<BaiTest> getBaiTestByTinTuyenDungId(Long tinTuyenDungId) {
        return baiTestRepository.findByTinTuyenDungId(tinTuyenDungId);
    }

    public BaiTest updateBaiTest(BaiTest baiTest) {
        if (!baiTestRepository.existsById(baiTest.getId())) {
            throw new RuntimeException("Không tìm thấy bài test!");
        }
        
        if (baiTest.getTieuDe() == null || baiTest.getTieuDe().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề không được để trống!");
        }
        if (baiTest.getLoai() == null || baiTest.getLoai().trim().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn loại bài test!");
        }
        if (baiTest.getTinTuyenDung() == null || baiTest.getTinTuyenDung().getId() == null) {
            throw new RuntimeException("Vui lòng chọn tin tuyển dụng!");
        }

        return baiTestRepository.save(baiTest);
    }

    public void deleteBaiTest(Long id) {
        if (!baiTestRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy bài test!");
        }
        baiTestRepository.deleteById(id);
    }

    public List<BaiTest> getBaiTestByCongtyIdSortedByTinTD(Long congtyId) {
        // Lấy danh sách bài test của công ty
        List<BaiTest> dsBaiTest = baiTestRepository.findAll().stream()
                .filter(baiTest -> baiTest.getTinTuyenDung() != null && 
                        baiTest.getTinTuyenDung().getCongty() != null &&
                        baiTest.getTinTuyenDung().getCongty().getId().equals(congtyId))
                .collect(Collectors.toList());
        
        // Sắp xếp theo ID tin tuyển dụng và ngày tạo (giảm dần)
        Collections.sort(dsBaiTest, Comparator
                .comparing((BaiTest b) -> b.getTinTuyenDung().getId())
                .thenComparing((BaiTest b) -> b.getNgayTao(), Comparator.reverseOrder()));
        
        return dsBaiTest;
    }

    // Nhóm bài test theo tin tuyển dụng để hiển thị
    public Map<TinTuyenDung, List<BaiTest>> getBaiTestGroupedByTinTD(Long congtyId) {
        // Lấy danh sách bài test đã sắp xếp
        List<BaiTest> dsBaiTest = getBaiTestByCongtyIdSortedByTinTD(congtyId);
        
        // Nhóm theo tin tuyển dụng
        Map<TinTuyenDung, List<BaiTest>> groupedTests = dsBaiTest.stream()
                .collect(Collectors.groupingBy(BaiTest::getTinTuyenDung));
        
        return groupedTests;
    }

    public Set<Long> getUniqueUngVienIds(List<BaiTest> baiTests) {
        return baiTests.stream()
            .filter(baiTest -> baiTest.getDSKQBaiTest() != null && !baiTest.getDSKQBaiTest().isEmpty())
            .flatMap(baiTest -> baiTest.getDSKQBaiTest().stream())
            .map(kq -> kq.getDonUngTuyen().getId())
            .collect(Collectors.toSet());
    }

    public int countUniqueUngVien(List<BaiTest> baiTests) {
        return getUniqueUngVienIds(baiTests).size();
    }

    
    public List<DonUngTuyen> getUniqueUngVien(List<BaiTest> baiTests) {
        Map<Long, DonUngTuyen> donMap = new HashMap<>();
        
        baiTests.forEach(baiTest -> {
            if (baiTest.getDSKQBaiTest() != null) {
                baiTest.getDSKQBaiTest().forEach(kq -> {
                    donMap.put(kq.getDonUngTuyen().getId(), kq.getDonUngTuyen());
                });
            }
        });
        
        return new ArrayList<>(donMap.values());
    }

}

