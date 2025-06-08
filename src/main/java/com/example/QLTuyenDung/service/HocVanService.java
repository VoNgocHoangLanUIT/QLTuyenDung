package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.HocVan;
import com.example.QLTuyenDung.repository.HocVanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HocVanService {
    private final HocVanRepository hocVanRepository;

    public List<HocVan> getHocVanByUserId(Long userId) {
        return hocVanRepository.findByUserIdOrderByNamKTDesc(userId);
    }

    public HocVan getHocVanById(Long id) {
        return hocVanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy học vấn với ID: " + id));
    }
    
    public HocVan saveHocVan(HocVan hocVan) {
        return hocVanRepository.save(hocVan);
    }
    
    public void deleteHocVan(Long id) {
        hocVanRepository.deleteById(id);
    }
    
}
