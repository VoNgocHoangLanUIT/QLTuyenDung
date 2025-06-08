package com.example.QLTuyenDung.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.ThanhTuu;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.ThanhTuuService;

import lombok.RequiredArgsConstructor;

import java.util.Date;

@Controller
@RequiredArgsConstructor
public class QLThanhTuuController {
    
    private final ThanhTuuService thanhTuuService;
    
    @GetMapping("/ungvien/add-thanhtuu")
    public String addThanhTuu(Model model) {
        ThanhTuu thanhTuu = new ThanhTuu();
        thanhTuu.setNgayDat(new Date()); // Set ngày hiện tại mặc định
        model.addAttribute("thanhTuu", thanhTuu);
        return "ungvien/QLThanhTuu/add";
    }
    
    @PostMapping("/ungvien/add-thanhtuu")
    public String addThanhTuu(@ModelAttribute("thanhTuu") ThanhTuu thanhTuu,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Gán user cho thành tựu
            thanhTuu.setUser(userHienTai);
            
            // Lưu thành tựu
            thanhTuuService.saveThanhTuu(thanhTuu);
            
            redirectAttributes.addFlashAttribute("success", "Thêm thành tựu thành công!");
            return "redirect:/ungvien/edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm thành tựu thất bại: " + e.getMessage());
            return "redirect:/ungvien/add-thanhtuu";
        }
    }
    
    @GetMapping("/ungvien/edit-thanhtuu/{id}")
    public String updateThanhTuu(@PathVariable Long id, 
                                      Model model, 
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            ThanhTuu thanhTuu = thanhTuuService.getThanhTuuById(id);
            
            if (!thanhTuu.getUser().getId().equals(userHienTai.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền sửa thành tựu này!");
                return "redirect:/ungvien/edit-user";
            }
            
            model.addAttribute("thanhTuu", thanhTuu);
            return "ungvien/QLThanhTuu/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thành tựu: " + e.getMessage());
            return "redirect:/ungvien/edit-user";
        }
    }
    
    @PostMapping("/ungvien/edit-thanhtuu")
    public String updateThanhTuu(@ModelAttribute("thanhTuu") ThanhTuu thanhTuu,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            ThanhTuu existingThanhTuu = thanhTuuService.getThanhTuuById(thanhTuu.getId());
            
            if (!existingThanhTuu.getUser().getId().equals(userHienTai.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền sửa thành tựu này!");
                return "redirect:/ungvien/edit-user";
            }
            
            // Giữ lại thông tin user
            thanhTuu.setUser(userHienTai);
            
            // Lưu thay đổi
            thanhTuuService.saveThanhTuu(thanhTuu);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành tựu thành công!");
            return "redirect:/ungvien/edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thành tựu thất bại: " + e.getMessage());
            return "redirect:/ungvien/edit-thanhtuu/" + thanhTuu.getId();
        }
    }
    
    @GetMapping("/ungvien/delete-thanhtuu/{id}")
    public String deleteThanhTuu(@PathVariable Long id, 
                                Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            ThanhTuu thanhTuu = thanhTuuService.getThanhTuuById(id);
            
            if (thanhTuu.getUser().getId().equals(userHienTai.getId())) {
                thanhTuuService.deleteThanhTuu(id);
                redirectAttributes.addFlashAttribute("success", "Xóa thành tựu thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa thành tựu này!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa thành tựu thất bại: " + e.getMessage());
        }
        
        return "redirect:/ungvien/edit-user";
    }
}