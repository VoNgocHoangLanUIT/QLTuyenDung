package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.ThanhTuu;
import com.example.QLTuyenDung.repository.ThanhTuuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThanhTuuService {
    private final ThanhTuuRepository thanhTuuRepository;

    public List<ThanhTuu> getThanhTuuByUserId(Long userId) {
        return thanhTuuRepository.findByUserIdOrderByNgayDatDesc(userId);
    }
    
    public ThanhTuu getThanhTuuById(Long id) {
        return thanhTuuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thành tựu với ID: " + id));
    }
    
    public ThanhTuu saveThanhTuu(ThanhTuu thanhTuu) {
        return thanhTuuRepository.save(thanhTuu);
    }
    
    public void deleteThanhTuu(Long id) {
        thanhTuuRepository.deleteById(id);
    }
    
}