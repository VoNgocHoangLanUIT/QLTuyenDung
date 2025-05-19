package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.BaiTest;

public interface BaiTestRepository extends JpaRepository<BaiTest, Long> {
    List<BaiTest> findByTinTuyenDungId(Long tinTuyenDungId);
}