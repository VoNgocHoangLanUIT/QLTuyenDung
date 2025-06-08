package com.example.QLTuyenDung.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.HocVan;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.HocVanService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLHocVanController {
    
    private final HocVanService hocVanService;
    
    @GetMapping("/ungvien/add-hocvan")
    public String addHocVan(Model model) {
        model.addAttribute("hocVan", new HocVan());
        return "ungvien/QLHocVan/add";
    }

    @PostMapping("/ungvien/add-hocvan")
    public String addHocVan(@ModelAttribute("hocVan") HocVan hocVan,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Gán user cho học vấn
            hocVan.setUser(userHienTai);
            
            // Lưu học vấn
            hocVanService.saveHocVan(hocVan);
            
            redirectAttributes.addFlashAttribute("success", "Thêm học vấn thành công!");
            return "redirect:/ungvien/edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm học vấn thất bại: " + e.getMessage());
            return "redirect:/ungvien/add-hocvan";
        }
    }
    @GetMapping("/ungvien/delete-hocvan/{id}")
    public String deleteHocVan(@PathVariable Long id, 
                            Authentication authentication, 
                            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            HocVan hocVan = hocVanService.getHocVanById(id);
            
            if (hocVan.getUser().getId().equals(userHienTai.getId())) {
                hocVanService.deleteHocVan(id);
                redirectAttributes.addFlashAttribute("success", "Xóa học vấn thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa học vấn này!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa học vấn thất bại: " + e.getMessage());
        }
        
        return "redirect:/ungvien/edit-user";
    }

    // Thêm phương thức hiển thị form sửa học vấn
    @GetMapping("/ungvien/edit-hocvan/{id}")
    public String updateHocVan(@PathVariable Long id, 
                                Model model, 
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            HocVan hocVan = hocVanService.getHocVanById(id);
            
            if (!hocVan.getUser().getId().equals(userHienTai.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền sửa học vấn này!");
                return "redirect:/ungvien/edit-user";
            }
            
            model.addAttribute("hocVan", hocVan);
            return "ungvien/QLHocVan/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy học vấn: " + e.getMessage());
            return "redirect:/ungvien/edit-user";
        }
    }

    @PostMapping("/ungvien/edit-hocvan")
    public String updateHocVan(@ModelAttribute("hocVan") HocVan hocVan,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User user = userDetails.getUser();
            HocVan existingHocVan = hocVanService.getHocVanById(hocVan.getId());
            
            if (!existingHocVan.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền sửa học vấn này!");
                return "redirect:/ungvien/edit-user";
            }
            
            // Giữ lại thông tin user
            hocVan.setUser(user);
            
            // Lưu thay đổi
            hocVanService.saveHocVan(hocVan);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật học vấn thành công!");
            return "redirect:/ungvien/edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật học vấn thất bại: " + e.getMessage());
            return "redirect:/ungvien/edit-hocvan/" + hocVan.getId();
        }
    }
}