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
public class BaiTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tieuDe;
    @Column(length = 4000)
    private String moTa;
    private String loai;
    private String linkGGForm;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayTao;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayDong;

    @ManyToOne
    @JoinColumn (name = "tintd_id", referencedColumnName = "id")
    private TinTuyenDung tinTuyenDung;

    @OneToMany(mappedBy = "baiTest", fetch = FetchType.EAGER)
    private Set<KQBaiTest> dSKQBaiTest;
}
