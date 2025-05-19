package com.example.QLTuyenDung.controller;

import java.util.Date;
import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.service.CongTyService;

import com.example.QLTuyenDung.service.TinTuyenDungService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/nhatd")
public class NhaTuyenDungController {

    @GetMapping("")
    public String chuyenHuongAdmin(){
        return "redirect:/nhatd/";
    }

    @GetMapping("/")
    public String admin(){
        return "nhatuyendung/dashboard";
    }
    
}
