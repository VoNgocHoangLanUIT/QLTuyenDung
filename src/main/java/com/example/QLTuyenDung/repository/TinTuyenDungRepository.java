package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.TinTuyenDung;

public interface TinTuyenDungRepository extends JpaRepository<TinTuyenDung, Long> {
    List<TinTuyenDung> findByCongty(CongTy congty);
    List<TinTuyenDung> findByTieuDeContainingIgnoreCaseAndTrangThai(String tuKhoa, String trangThai);
    List<TinTuyenDung> findByThanhPhoLVContainingIgnoreCaseAndTrangThai(String diaDiem, String trangThai);
    List<TinTuyenDung> findByTieuDeContainingIgnoreCaseAndThanhPhoLVContainingIgnoreCaseAndTrangThai(
        String tuKhoa, String diaDiem, String trangThai);
    List<TinTuyenDung> findByTrangThai(String trangThai);
    List<TinTuyenDung> findByTrangThaiOrderByNgayDangDesc(String trangThai, Pageable pageable);
    Page<TinTuyenDung> findAllByTrangThai(String trangThai, Pageable pageable);
    Page<TinTuyenDung> findByTieuDeContainingIgnoreCaseAndTrangThai(String tuKhoa, String trangThai, Pageable pageable);
    Page<TinTuyenDung> findByThanhPhoLVContainingIgnoreCaseAndTrangThai(String diaDiem, String trangThai, Pageable pageable);
    Page<TinTuyenDung> findByTieuDeContainingIgnoreCaseAndThanhPhoLVContainingIgnoreCaseAndTrangThai(
        String tuKhoa, String diaDiem, String trangThai, Pageable pageable);
}
