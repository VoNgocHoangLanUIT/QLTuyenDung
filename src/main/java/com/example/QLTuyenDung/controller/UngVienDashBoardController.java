package com.example.QLTuyenDung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ungvien")
public class UngVienDashBoardController {
    @GetMapping("")
    public String chuyenHuongUngVien(){
        return "redirect:/ungvien/";
    }

    @GetMapping("/")
    public String ungVien(){
        return "ungvien/dashboard";
    }
}
