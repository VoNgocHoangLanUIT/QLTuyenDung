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
public class DonUngTuyen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayUngTuyen;
    private String trangThai;
    private boolean quyenTest;
    @NotNull(message = "CV không được để trống")
    @Column(name = "cv_file")
    private String cvFile;

    @ManyToOne
    @JoinColumn (name = "user_id", referencedColumnName = "id")
    private User user;

    @NotNull(message = "Tin tuyển dụng không được để trống")
    @ManyToOne
    @JoinColumn (name = "tintd_id", referencedColumnName = "id")
    private TinTuyenDung tinTuyenDung;

    @OneToMany(mappedBy = "donUngTuyen", fetch = FetchType.EAGER)
    private Set<KQBaiTest> dSKQBaiTest;

    @OneToMany(mappedBy = "donUngTuyen", fetch = FetchType.EAGER)
    private Set<PhongVan> dSPhongVan;
}
