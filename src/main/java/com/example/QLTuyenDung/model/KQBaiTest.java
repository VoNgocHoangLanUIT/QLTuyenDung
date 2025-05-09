package com.example.QLTuyenDung.model;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class KQBaiTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int diem;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date ngayLam;

    @NotNull(message = "Đơn ứng tuyển không được để trống")
    @ManyToOne
    @JoinColumn(name = "donut_id", referencedColumnName = "id")
    private DonUngTuyen donUngTuyen;

    @NotNull(message = "Bài test không được để trống")
    @ManyToOne
    @JoinColumn (name = "test_id", referencedColumnName = "id")
    private BaiTest baiTest;
}