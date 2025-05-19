package com.example.QLTuyenDung.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.TinTuyenDung;

public interface TinTuyenDungRepository extends JpaRepository<TinTuyenDung, Long> {
    List<TinTuyenDung> findByCongty(CongTy congty);
}
