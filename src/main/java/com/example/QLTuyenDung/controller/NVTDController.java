package com.example.QLTuyenDung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/nvtd")
public class NVTDController {
    @GetMapping("")
    public String chuyenHuongNVTD(){
        return "redirect:/nvtd/";
    }

    @GetMapping("/")
    public String nhanVienTuyenDung(){
        return "nvtd/dashboard";
    }
}
