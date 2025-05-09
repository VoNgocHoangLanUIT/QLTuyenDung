package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.repository.CongTyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CongTyService {
    private final CongTyRepository congTyRepository;
    
    public List<CongTy> getAllCongTy() {
        return congTyRepository.findAll();
    }

    public CongTy getCongTyById(Long id) {
        return congTyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy công ty!"));
    }

    public CongTy addCongTy(CongTy congTy) {
        if (congTy.getTenCongTy() == null || congTy.getTenCongTy().trim().isEmpty()) {
            throw new RuntimeException("Tên công ty không được để trống!");
        }
        return congTyRepository.save(congTy);
    }

    public CongTy updateCongTy(CongTy congTy) {
        if (!congTyRepository.existsById(congTy.getId())) {
            throw new RuntimeException("Không tìm thấy công ty!");
        }
        return congTyRepository.save(congTy);
    }

    public void deleteCongTy(Long id) {
        if (!congTyRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy công ty!");
        }
        congTyRepository.deleteById(id);
    }
}
