package com.example.QLTuyenDung.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.KinhNghiemLamViec;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.KinhNghiemLamViecService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLKinhNghiemController {
    
    private final KinhNghiemLamViecService kinhNghiemLamViecService;
    
    @GetMapping("/ungvien/add-kinhnghiem")
    public String addKinhNghiem(Model model) {
        model.addAttribute("kinhNghiem", new KinhNghiemLamViec());
        return "ungvien/QLKinhNghiem/add";
    }
    
    @PostMapping("/ungvien/add-kinhnghiem")
    public String addKinhNghiem(@ModelAttribute("kinhNghiem") KinhNghiemLamViec kinhNghiem,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin user hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Gán user cho kinh nghiệm làm việc
            kinhNghiem.setUser(userHienTai);
            
            // Lưu kinh nghiệm làm việc
            kinhNghiemLamViecService.saveKinhNghiemLamViec(kinhNghiem);
            
            redirectAttributes.addFlashAttribute("success", "Thêm kinh nghiệm làm việc thành công!");
            return "redirect:/ungvien/edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Thêm kinh nghiệm làm việc thất bại: " + e.getMessage());
            return "redirect:/ungvien/add-kinhnghiem";
        }
    }
    
    @GetMapping("/ungvien/edit-kinhnghiem/{id}")
    public String updateKinhNghiem(@PathVariable Long id, 
                                      Model model, 
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            KinhNghiemLamViec kinhNghiem = kinhNghiemLamViecService.getKinhNghiemLamViecById(id);
            
            if (!kinhNghiem.getUser().getId().equals(userHienTai.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền sửa kinh nghiệm làm việc này!");
                return "redirect:/ungvien/edit-user";
            }
            
            model.addAttribute("kinhNghiem", kinhNghiem);
            return "ungvien/QLKinhNghiem/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy kinh nghiệm làm việc: " + e.getMessage());
            return "redirect:/ungvien/edit-user";
        }
    }
    
    @PostMapping("/ungvien/edit-kinhnghiem")
    public String updateKinhNghiem(@ModelAttribute("kinhNghiem") KinhNghiemLamViec kinhNghiem,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            KinhNghiemLamViec existingKinhNghiem = kinhNghiemLamViecService.getKinhNghiemLamViecById(kinhNghiem.getId());
            
            if (!existingKinhNghiem.getUser().getId().equals(userHienTai.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền sửa kinh nghiệm làm việc này!");
                return "redirect:/ungvien/edit-user";
            }
            
            // Giữ lại thông tin user
            kinhNghiem.setUser(userHienTai);
            
            // Lưu thay đổi
            kinhNghiemLamViecService.saveKinhNghiemLamViec(kinhNghiem);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật kinh nghiệm làm việc thành công!");
            return "redirect:/ungvien/edit-user";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật kinh nghiệm làm việc thất bại: " + e.getMessage());
            return "redirect:/ungvien/edit-kinhnghiem/" + kinhNghiem.getId();
        }
    }
    
    @GetMapping("/ungvien/delete-kinhnghiem/{id}")
    public String deleteKinhNghiem(@PathVariable Long id, 
                                 Authentication authentication, 
                                 RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra quyền sở hữu
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            KinhNghiemLamViec kinhNghiem = kinhNghiemLamViecService.getKinhNghiemLamViecById(id);
            
            if (kinhNghiem.getUser().getId().equals(userHienTai.getId())) {
                kinhNghiemLamViecService.deleteKinhNghiemLamViec(id);
                redirectAttributes.addFlashAttribute("success", "Xóa kinh nghiệm làm việc thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa kinh nghiệm làm việc này!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Xóa kinh nghiệm làm việc thất bại: " + e.getMessage());
        }
        
        return "redirect:/ungvien/edit-user";
    }
}