package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.QLTuyenDung.model.ThanhTuu;
import com.example.QLTuyenDung.repository.ThanhTuuRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThanhTuuService {
    private final ThanhTuuRepository thanhTuuRepository;

    public List<ThanhTuu> getThanhTuuByUserId(Long userId) {
        return thanhTuuRepository.findByUserIdOrderByNgayDatDesc(userId);
    }
}
