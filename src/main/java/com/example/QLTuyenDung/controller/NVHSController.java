package com.example.QLTuyenDung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/nvhs")
public class NVHSController {
    @GetMapping("")
    public String chuyenHuongNVHS(){
        return "redirect:/nvhs/";
    }

    @GetMapping("/")
    public String nhanVienHoSo(){
        return "nvhs/dashboard";
    }
}
