package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.HocVan;

public interface HocVanRepository extends JpaRepository<HocVan, Long> {
    List<HocVan> findByUserId(Long userId);
    List<HocVan> findByUserIdOrderByNamKTDesc(Long userId);
}
