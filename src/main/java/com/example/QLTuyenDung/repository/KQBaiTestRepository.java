package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.KQBaiTest;

public interface KQBaiTestRepository extends JpaRepository<KQBaiTest, Long> {
    boolean existsByDonUngTuyenIdAndBaiTestId(Long donUngTuyenId, Long baiTestId);
    List<KQBaiTest> findByBaiTestId(Long baiTestId);
    long countByDonUngTuyenIdAndIdNot(Long donUngTuyenId, Long kqBaiTestId);
}