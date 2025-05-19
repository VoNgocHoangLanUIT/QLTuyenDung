package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.KinhNghiemLamViec;

public interface KinhNghiemLamViecRepository extends JpaRepository<KinhNghiemLamViec, Long> {
    List<KinhNghiemLamViec> findByUserId(Long userId);
    List<KinhNghiemLamViec> findByUserIdOrderByNamKTDesc(Long userId);
}
