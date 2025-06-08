package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QLTuyenDung.model.TinYeuThich;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.repository.TinYeuThichRepository;
import com.example.QLTuyenDung.repository.TinTuyenDungRepository;
import com.example.QLTuyenDung.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TinYeuThichService {
    
    private final TinYeuThichRepository tinYeuThichRepository;
    private final UserRepository userRepository;
    private final TinTuyenDungRepository tinTuyenDungRepository;
    
    @Transactional
    public boolean toggleBookmark(Long ungVienId, Long tinTuyenDungId) {
        // Kiểm tra xem đã bookmark chưa
        TinYeuThich existing = tinYeuThichRepository.findByUngVienIdAndTinTuyenDungId(ungVienId, tinTuyenDungId);
            
        if (existing != null) {
            // Nếu đã bookmark, xóa bookmark
            tinYeuThichRepository.delete(existing);
            return false;
        } else {
            // Nếu chưa bookmark, thêm bookmark mới
            User ungVien = userRepository.findById(ungVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ứng viên"));
                
            TinTuyenDung tinTuyenDung = tinTuyenDungRepository.findById(tinTuyenDungId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tin tuyển dụng"));
                
            TinYeuThich newBookmark = new TinYeuThich();
            newBookmark.setUngVien(ungVien);
            newBookmark.setTinTuyenDung(tinTuyenDung);
            
            tinYeuThichRepository.save(newBookmark);
            return true;
        }
    }
    
    public boolean isBookmarked(Long ungVienId, Long tinTuyenDungId) {
        return tinYeuThichRepository.existsByUngVienIdAndTinTuyenDungId(ungVienId, tinTuyenDungId);
    }

    public List<TinYeuThich> getDSTinYeuThichByUngVienID(Long ungVienId) {
        return tinYeuThichRepository.findByUngVienId(ungVienId);
    }

    @Transactional
    public void deleteTinYeuThich(Long id, Long ungVienId) {
        TinYeuThich bookmark = tinYeuThichRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh dấu yêu thích"));
            
        // Kiểm tra xem bookmark này có thuộc về ứng viên không
        if (!bookmark.getUngVien().getId().equals(ungVienId)) {
            throw new RuntimeException("Bạn không có quyền xóa đánh dấu yêu thích này");
        }
        
        tinYeuThichRepository.delete(bookmark);
    }
}