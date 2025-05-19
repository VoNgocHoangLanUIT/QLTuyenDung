package com.example.QLTuyenDung.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class PhongVan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date ngayPV;
    private String diaDiem;
    private String trangThai;
    private int diemDanhGia;
    @Column(length = 4000)
    private String nhanXet;

    @ManyToOne
    @JoinColumn(name = "donut_id", referencedColumnName = "id")
    private DonUngTuyen donUngTuyen;

    @ManyToOne
    @JoinColumn(name = "nhanvien_id", referencedColumnName = "id") 
    private User nhanVienTD;
}
