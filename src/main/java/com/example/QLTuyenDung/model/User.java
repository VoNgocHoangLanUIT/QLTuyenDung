package com.example.QLTuyenDung.model;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    @Column(name = "enabled", columnDefinition = "NUMBER(1,0) DEFAULT 1")
    private Boolean enabled = true;
    private String hoTen;
    private String soDienThoai;
    @NotEmpty(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Column(unique = true)
    private String email;
    private String chuyenNganh;
    private int namKinhNghiem;
    private int tuoi;
    private int luongHienTai;
    private int luongMongMuon;
    private String chungChi;
    private String ngonNgu;
    private String gioiTinh;
    private String gioiThieu;
    private String faceBook;
    private String linkedIn;
    private String diaChi;
    private String hinhAnh;
    private Date ngayTao;
    @Column(name = "cv_file")
    private String cvFile;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserRole> userRoles;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<DonUngTuyen> dSDonUngTuyen;

    @OneToMany(mappedBy = "nhanVienTD", fetch = FetchType.EAGER)
    private Set<PhongVan> dSPhongVan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cong_ty_id", referencedColumnName = "id")
    private CongTy congTy;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<HocVan> dSHocVan;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<KinhNghiemLamViec> dSKinhNghiemLamViec;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ThanhTuu> dSThanhTuu;

    @OneToMany(mappedBy = "ungVien", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UngVienYeuThich> dSUngVienYeuThich;

    @OneToMany(mappedBy = "nhaTuyenDung", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UngVienYeuThich> dSNhaTDYeuThich;
}
