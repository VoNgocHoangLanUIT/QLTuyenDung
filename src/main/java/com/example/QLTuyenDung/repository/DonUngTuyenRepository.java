package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.DonUngTuyen;


public interface DonUngTuyenRepository extends JpaRepository<DonUngTuyen, Long>  {
    boolean existsByUserIdAndTinTuyenDungId(Long userId, Long tinTuyenDungId);
    List<DonUngTuyen> findByTinTuyenDungId(Long tinTuyenDungId);
    List<DonUngTuyen> findByTrangThaiAndTinTuyenDungId(String trangThai, Long tinTuyenDungId);
    List<DonUngTuyen> findByQuyenTestAndTinTuyenDungId(boolean quyenTest, Long tinTuyenDungId);
    List<DonUngTuyen> findByUserIdAndTinTuyenDungId(Long userId, Long tinTuyenDungId);
}
