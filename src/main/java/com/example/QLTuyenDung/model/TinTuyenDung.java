package com.example.QLTuyenDung.model;

import java.util.Date;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class TinTuyenDung {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tieuDe;
    @Column(length = 4000)
    private String moTaCongViec;
    private String linhVuc;
    private String hinhThucLV;
    private String thanhPhoLV;
    private String quocGiaLV;
    private String diaDiemLV;
    private int mucLuong;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date hanNop;
    @Column(length = 4000)
    private String yeuCau;
    private String trangThai;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayDang;

    @ManyToOne
    @JoinColumn (name = "congty_id", referencedColumnName = "id")
    private CongTy congty;

    @OneToMany(mappedBy = "tinTuyenDung", fetch = FetchType.EAGER)
    private Set<DonUngTuyen> dSDonUngTuyen;

    @OneToMany(mappedBy = "tinTuyenDung", fetch = FetchType.EAGER)
    private Set<BaiTest> dSBaiTest;
}
