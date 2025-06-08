package com.example.QLTuyenDung.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.TinYeuThich;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.TinYeuThichService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyTinYeuThichController {
    
    private final TinYeuThichService tinYeuThichService;
    
    @PostMapping("/ungvien/toggle-bookmark/{tinTDId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long tinTDId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User ungVien = userDetails.getUser();
            
            // Kiểm tra xem người dùng có quyền ứng viên không
            boolean hasUngVienRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("CANDIDATE"));
                
            if (!hasUngVienRole) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền thực hiện chức năng này");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean isBookmarked = tinYeuThichService.toggleBookmark(ungVien.getId(), tinTDId);
            
            response.put("success", true);
            response.put("bookmarked", isBookmarked);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/ungvien/tintd-yeuthich")
    public String showDSTinYeuThich(Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User ungVien = userDetails.getUser();
            
            // Kiểm tra xem người dùng có quyền ứng viên không
            boolean hasUngVienRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("CANDIDATE"));
                
            if (!hasUngVienRole) {
                return "redirect:/";
            }
            
            // Lấy danh sách tin tuyển dụng yêu thích
            List<TinYeuThich> danhSachYeuThich = tinYeuThichService.getDSTinYeuThichByUngVienID(ungVien.getId());
            
            model.addAttribute("danhSachYeuThich", danhSachYeuThich);
            
            return "ungvien/TinYeuThich";
        } catch (Exception e) {
            return "redirect:/";
        }
    }
    
    @GetMapping("/ungvien/xoa-tintd-yeuthich/{id}")
    public String removeTinYeuThich(@PathVariable Long id, 
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User ungVien = userDetails.getUser();
            
            // Xóa bookmark
            tinYeuThichService.deleteTinYeuThich(id, ungVien.getId());
            
            redirectAttributes.addFlashAttribute("success", "Đã xóa tin tuyển dụng khỏi danh sách yêu thích");
            return "redirect:/ungvien/tintd-yeuthich";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/ungvien/tintd-yeuthich";
        }
    }
}