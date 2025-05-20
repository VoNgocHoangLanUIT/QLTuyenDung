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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.UngVienYeuThich;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.UngVienYeuThichService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyUngVienYeuThichController {
    
    private final UngVienYeuThichService ungVienYeuThichService;
    
    @PostMapping("/nhatd/toggle-bookmark/{ungVienId}")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @PathVariable Long ungVienId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User nhaTuyenDung = userDetails.getUser();
            
            // Kiểm tra xem người dùng có quyền nhà tuyển dụng không
            boolean hasNhaTDRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("RECRUITER"));
                
            if (!hasNhaTDRole) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền thực hiện chức năng này");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean isBookmarked = ungVienYeuThichService.toggleBookmark(nhaTuyenDung.getId(), ungVienId);
            
            response.put("success", true);
            response.put("bookmarked", isBookmarked);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/nhatd/ungvien-yeuthich")
    public String showDSUngVienYeuThich(Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User nhaTuyenDung = userDetails.getUser();
            
            // Kiểm tra xem người dùng có quyền nhà tuyển dụng không
            boolean hasNhaTDRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("RECRUITER"));
                
            if (!hasNhaTDRole) {
                return "redirect:/";
            }
            
            // Lấy danh sách ứng viên yêu thích
            List<UngVienYeuThich> danhSachYeuThich = ungVienYeuThichService.getDSUngVienYeuThichByNhaTDID(nhaTuyenDung.getId());
            
            model.addAttribute("danhSachYeuThich", danhSachYeuThich);
            
            return "nhatuyendung/UngVienYeuThich";
        } catch (Exception e) {
            return "redirect:/";
        }
    }
    
    @GetMapping("/nhatd/xoa-ungvien-yeuthich/{id}")
    public String removeUngVienYeuThich(@PathVariable Long id, 
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User nhaTuyenDung = userDetails.getUser();
            
            // Xóa bookmark
            ungVienYeuThichService.deleteUngVienYeuThich(id, nhaTuyenDung.getId());
            
            redirectAttributes.addFlashAttribute("success", "Đã xóa ứng viên khỏi danh sách yêu thích");
            return "redirect:/nhatd/ungvien-yeuthich";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/ungvien-yeuthich";
        }
    }
}