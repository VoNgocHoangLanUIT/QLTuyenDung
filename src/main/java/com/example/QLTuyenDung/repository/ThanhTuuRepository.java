package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.ThanhTuu;

public interface ThanhTuuRepository extends JpaRepository<ThanhTuu, Long> {
    List<ThanhTuu> findByUserId(Long userId);
    List<ThanhTuu> findByUserIdOrderByNgayDatDesc(Long userId);

}
