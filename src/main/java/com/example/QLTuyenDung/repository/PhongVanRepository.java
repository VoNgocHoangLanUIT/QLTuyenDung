package com.example.QLTuyenDung.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.model.User;

public interface PhongVanRepository extends JpaRepository<PhongVan, Long> {
    List<PhongVan> findByDonUngTuyen(DonUngTuyen donUngTuyen);
    List<PhongVan> findByNhanVienTD(User nhanVienTD);
}