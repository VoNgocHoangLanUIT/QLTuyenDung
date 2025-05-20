package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.QLTuyenDung.model.UngVienYeuThich;

@Repository
public interface UngVienYeuThichRepository extends JpaRepository<UngVienYeuThich, Long> {
    
    UngVienYeuThich findByNhaTuyenDungIdAndUngVienId(Long nhaTuyenDungId, Long ungVienId);
    
    boolean existsByNhaTuyenDungIdAndUngVienId(Long nhaTuyenDungId, Long ungVienId);
    
    List<UngVienYeuThich> findByNhaTuyenDungId(Long nhaTuyenDungId);
    
    void deleteByNhaTuyenDungIdAndUngVienId(Long nhaTuyenDungId, Long ungVienId);
}