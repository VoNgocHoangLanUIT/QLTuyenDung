package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.BaiTest;
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
}

