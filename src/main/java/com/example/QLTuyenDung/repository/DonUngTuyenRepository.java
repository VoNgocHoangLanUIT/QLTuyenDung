package com.example.QLTuyenDung.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.DonUngTuyen;


public interface DonUngTuyenRepository extends JpaRepository<DonUngTuyen, Long>  {
    boolean existsByUserIdAndTinTuyenDungId(Long userId, Long tinTuyenDungId);
}
