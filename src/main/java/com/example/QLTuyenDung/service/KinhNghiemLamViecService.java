package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.KinhNghiemLamViec;
import com.example.QLTuyenDung.repository.KinhNghiemLamViecRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KinhNghiemLamViecService {
    private final KinhNghiemLamViecRepository kinhNghiemLamViecRepository;

    public List<KinhNghiemLamViec> getKinhNghiemLamViecByUserId(Long userId) {
        return kinhNghiemLamViecRepository.findByUserIdOrderByNamKTDesc(userId);
    }

    public KinhNghiemLamViec getKinhNghiemLamViecById(Long id) {
        return kinhNghiemLamViecRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kinh nghiệm làm việc với ID: " + id));
    }
    
    public KinhNghiemLamViec saveKinhNghiemLamViec(KinhNghiemLamViec kinhNghiemLamViec) {
        return kinhNghiemLamViecRepository.save(kinhNghiemLamViec);
    }
    
    public void deleteKinhNghiemLamViec(Long id) {
        kinhNghiemLamViecRepository.deleteById(id);
    }
    
}
