package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.User;
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
        if (tinTuyenDung.getTieuDe() == null || tinTuyenDung.getTieuDe().trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề tuyển dụng không được để trống!");
        }
        tinTuyenDung.setTrangThai("dangtuyen");
        tinTuyenDung.setNgayDang(new Date());
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
    
    public List<TinTuyenDung> getTinTuyenDungByCongTy(CongTy congTy) {
        try {
            // Thực hiện query để lấy tin tuyển dụng theo công ty
            return tinTuyenDungRepository.findByCongty(congTy);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tin tuyển dụng theo công ty: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public Page<TinTuyenDung> getAllTinTuyenDungPaginated(int page, int size, String sortField, String sortDirection) {
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return tinTuyenDungRepository.findAll(pageable);
    }

    public Page<TinTuyenDung> getTinTuyenDungByTrangThaiPaginated(String trangThai, int page, int size, 
                                                                String sortField, String sortDirection) {
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        return tinTuyenDungRepository.findAllByTrangThai(trangThai, pageable);
    }

    public Page<TinTuyenDung> timKiemTinTuyenDungPaginated(String tuKhoa, String diaDiem, int page, int size,
                                                        String sortField, String sortDirection) {
        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Nếu không có từ khóa và địa điểm, trả về tất cả tin đang tuyển
        if ((tuKhoa == null || tuKhoa.trim().isEmpty()) && 
            (diaDiem == null || diaDiem.trim().isEmpty())) {
            return tinTuyenDungRepository.findAllByTrangThai("dangtuyen", pageable);
        }
        
        // Tìm theo từ khóa hoặc địa điểm hoặc cả hai
        if (diaDiem == null || diaDiem.trim().isEmpty()) {
            return tinTuyenDungRepository.findByTieuDeContainingIgnoreCaseAndTrangThai(tuKhoa, "dangtuyen", pageable);
        }
        
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            return tinTuyenDungRepository.findByThanhPhoLVContainingIgnoreCaseAndTrangThai(diaDiem, "dangtuyen", pageable);
        }
        
        return tinTuyenDungRepository.findByTieuDeContainingIgnoreCaseAndThanhPhoLVContainingIgnoreCaseAndTrangThai(
            tuKhoa, diaDiem, "dangtuyen", pageable);
    }

    public List<TinTuyenDung> timKiemTinTuyenDung(String tuKhoa, String diaDiem) {
        try {
            // Nếu không có từ khóa và địa điểm, trả về tất cả tin
            if ((tuKhoa == null || tuKhoa.trim().isEmpty()) && 
                (diaDiem == null || diaDiem.trim().isEmpty())) {
                return tinTuyenDungRepository.findByTrangThai("dangtuyen");
            }
            
            // Tìm theo từ khóa hoặc địa điểm hoặc cả hai
            if (diaDiem == null || diaDiem.trim().isEmpty()) {
                return tinTuyenDungRepository.findByTieuDeContainingIgnoreCaseAndTrangThai(tuKhoa, "dangtuyen");
            }
            
            if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
                return tinTuyenDungRepository.findByThanhPhoLVContainingIgnoreCaseAndTrangThai(diaDiem, "dangtuyen");
            }
            
            return tinTuyenDungRepository.findByTieuDeContainingIgnoreCaseAndThanhPhoLVContainingIgnoreCaseAndTrangThai(
                tuKhoa, diaDiem, "dangtuyen");
        } catch (Exception e) {
            System.err.println("Lỗi khi tìm kiếm tin tuyển dụng: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<TinTuyenDung> getLatestJobs(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            return tinTuyenDungRepository.findByTrangThaiOrderByNgayDangDesc("dangtuyen", pageable);
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tin tuyển dụng mới nhất: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
