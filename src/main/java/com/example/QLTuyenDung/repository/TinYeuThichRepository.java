package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.QLTuyenDung.model.TinYeuThich;

@Repository
public interface TinYeuThichRepository extends JpaRepository<TinYeuThich, Long> {
    TinYeuThich findByUngVienIdAndTinTuyenDungId(Long ungVienId, Long tinTuyenDungId);
    boolean existsByUngVienIdAndTinTuyenDungId(Long ungVienId, Long tinTuyenDungId);
    List<TinYeuThich> findByUngVienId(Long ungVienId);
}