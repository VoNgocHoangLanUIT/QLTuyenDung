package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.repository.TinTuyenDungRepository;

@Service
@RequiredArgsConstructor
public class TinTuyenDungService {
    
    private final TinTuyenDungRepository tinTuyenDungRepository;
    
    public List<TinTuyenDung> getAllTinTuyenDung() {
        try {
            return tinTuyenDungRepository.findAll();
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public TinTuyenDung addTinTuyenDung(TinTuyenDung tinTuyenDung) {
        // Add validation if needed
        if (tinTuyenDung.getViTri() == null || tinTuyenDung.getViTri().trim().isEmpty()) {
            throw new RuntimeException("Vị trí tuyển dụng không được để trống!");
        }
        tinTuyenDung.setTrangThai("dangtuyen");
        return tinTuyenDungRepository.save(tinTuyenDung);
    }

    public TinTuyenDung getTinTuyenDungById(Long id) {
        return tinTuyenDungRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng!"));
    }

    public TinTuyenDung updateTinTuyenDung(TinTuyenDung tinTuyenDung) {
        if (!tinTuyenDungRepository.existsById(tinTuyenDung.getId())) {
            throw new RuntimeException("Không tìm thấy tin tuyển dụng!");
        }
        return tinTuyenDungRepository.save(tinTuyenDung);
    }

    public void deleteTinTuyenDung(Long id) {
        if (!tinTuyenDungRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy tin tuyển dụng!");
        }
        tinTuyenDungRepository.deleteById(id);
    }
}
