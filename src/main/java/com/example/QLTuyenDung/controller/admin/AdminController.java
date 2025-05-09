package com.example.QLTuyenDung.controller.admin;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;



import lombok.RequiredArgsConstructor;



@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("")
    public String chuyenHuongAdmin(){
        return "redirect:/admin/";
    }

    @GetMapping("/")
    public String admin(){
        return "admin/index";
    }

    
}
