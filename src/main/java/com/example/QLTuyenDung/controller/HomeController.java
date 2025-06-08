package com.example.QLTuyenDung.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.TinYeuThich;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.TinTuyenDungService;
import com.example.QLTuyenDung.service.TinYeuThichService;
import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("")
public class HomeController {
    private final UserService userService;
    private final TinTuyenDungService tinTuyenDungService;
    private final TinYeuThichService tinYeuThichService;
    @GetMapping("")
    public String user(Model model, Authentication authentication){
        List<TinTuyenDung> latestJobs = tinTuyenDungService.getLatestJobs(5);
        model.addAttribute("latestJobs", latestJobs);
        
        // Thêm thông tin bookmark nếu user đã đăng nhập
        if (authentication != null && authentication.isAuthenticated()) {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            boolean isCandidate = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("CANDIDATE"));
                
            if (isCandidate) {
                List<TinYeuThich> bookmarks = tinYeuThichService.getDSTinYeuThichByUngVienID(user.getId());
                List<Long> bookmarkIds = bookmarks.stream()
                    .map(bookmark -> bookmark.getTinTuyenDung().getId())
                    .collect(Collectors.toList());
                
                model.addAttribute("userBookmarks", bookmarkIds);
            }
        }
        
        return "user/index";
    }

    @GetMapping("/login")
    public String userLogin(){
        return "logon";
    }

    @PostMapping("/register")
    public String userRegister(@RequestParam String username, @RequestParam String email, @RequestParam String password, @RequestParam String roleName, RedirectAttributes redirectAttributes)  {
        try {
            userService.registerUser(username, email, password, roleName);
            return "redirect:/login?register=true";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("registerError", true);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login?register=false";
        }
    }

    @GetMapping("/doi-password")
    public String showChangePasswordForm() {
        return "user/DoiPassword";
    }
    
    // Xử lý yêu cầu đổi mật khẩu
    @PostMapping("/doi-password")
    public String processChangePassword(
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User currentUser = userDetails.getUser();
            
            // Kiểm tra mật khẩu xác nhận có khớp không
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/doi-password";
            }
            
            // Thực hiện đổi mật khẩu
            boolean success = userService.changePassword(currentUser.getId(), oldPassword, newPassword);
            
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được thay đổi thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
            }
            
            return "redirect:/doi-password";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/doi-password";
        }
    }
}
