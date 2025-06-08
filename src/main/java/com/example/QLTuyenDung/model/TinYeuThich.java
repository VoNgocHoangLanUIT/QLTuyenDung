package com.example.QLTuyenDung.model;

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
public class TinYeuThich {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn (name = "ung_vien_id", referencedColumnName = "id")
    private User ungVien;

    @ManyToOne
    @JoinColumn (name = "tintd_id", referencedColumnName = "id")
    private TinTuyenDung tinTuyenDung;
}
