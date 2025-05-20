package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QLTuyenDung.model.UngVienYeuThich;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.repository.UngVienYeuThichRepository;
import com.example.QLTuyenDung.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UngVienYeuThichService {

    private final UngVienYeuThichRepository ungVienYeuThichRepository;
    private final UserRepository userRepository;
    
    /**
     * Toggle bookmark status for a candidate
     * @param nhaTuyenDungId - ID of the recruiter
     * @param ungVienId - ID of the candidate
     * @return true if bookmarked, false if unbookmarked
     */
    @Transactional
    public boolean toggleBookmark(Long nhaTuyenDungId, Long ungVienId) {
        // Kiểm tra xem đã bookmark chưa
        UngVienYeuThich existing = ungVienYeuThichRepository.findByNhaTuyenDungIdAndUngVienId(nhaTuyenDungId, ungVienId);
            
        if (existing != null) {
            // Nếu đã bookmark, xóa bookmark
            ungVienYeuThichRepository.delete(existing);
            return false;
        } else {
            // Nếu chưa bookmark, thêm bookmark mới
            User nhaTuyenDung = userRepository.findById(nhaTuyenDungId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhà tuyển dụng"));
                
            User ungVien = userRepository.findById(ungVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ứng viên"));
                
            UngVienYeuThich newBookmark = new UngVienYeuThich();
            newBookmark.setNhaTuyenDung(nhaTuyenDung);
            newBookmark.setUngVien(ungVien);
            
            ungVienYeuThichRepository.save(newBookmark);
            return true;
        }
    }
    
    public boolean isBookmarked(Long nhaTuyenDungId, Long ungVienId) {
        return ungVienYeuThichRepository.existsByNhaTuyenDungIdAndUngVienId(nhaTuyenDungId, ungVienId);
    }

    public List<UngVienYeuThich> getDSUngVienYeuThichByNhaTDID(Long nhaTuyenDungId) {
        return ungVienYeuThichRepository.findByNhaTuyenDungId(nhaTuyenDungId);
    }

    @Transactional
    public void deleteUngVienYeuThich(Long id, Long nhaTuyenDungId) {
        UngVienYeuThich bookmark = ungVienYeuThichRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh dấu yêu thích"));
            
        // Kiểm tra xem bookmark này có thuộc về nhà tuyển dụng không
        if (!bookmark.getNhaTuyenDung().getId().equals(nhaTuyenDungId)) {
            throw new RuntimeException("Bạn không có quyền xóa đánh dấu yêu thích này");
        }
        
        ungVienYeuThichRepository.delete(bookmark);
    }
}