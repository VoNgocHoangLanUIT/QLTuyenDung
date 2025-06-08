package com.example.QLTuyenDung.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.CongTyService;
import com.example.QLTuyenDung.service.FileStorageService;
import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyCongTyController {
    private final CongTyService congTyService;
    private final FileStorageService fileStorageService;
    private final UserService userService;

    @GetMapping("/admin/dscongty")
    public String adminShowDSCongTy(Model model) {
        List<CongTy> dsCongTy = congTyService.getAllCongTy();
        model.addAttribute("dsCongTy", dsCongTy);
        return "admin/QLCongTy/index";
    }

    @GetMapping("/dscongty")
    public String showDSCongTy(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "tenCongTy") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Page<CongTy> pageCongTy = congTyService.getAllCongTyPaginated(page, size, sort, direction);
        List<CongTy> dsCongTy = pageCongTy.getContent();
        
        model.addAttribute("dsCongTy", dsCongTy);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageCongTy.getTotalPages());
        model.addAttribute("totalItems", pageCongTy.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("reverseSortDir", direction.equals("asc") ? "desc" : "asc");
        
        // Thông tin cho phân trang
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(pageCongTy.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        
        return "user/CongTy/index";
    }

    @GetMapping("/admin/add-congty")
    public String adminAddCongTy(Model model) {
        model.addAttribute("congTy", new CongTy());
        return "admin/QLCongTy/add";
    }

    @PostMapping("/admin/add-congty")
    public String adminAddCongTy(@ModelAttribute CongTy congTy,
                           RedirectAttributes redirectAttributes) {
        try {
            congTyService.addCongTy(congTy);
            redirectAttributes.addFlashAttribute("success", "Thêm công ty thành công!");
            return "redirect:/admin/dscongty";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/add-congty";
        }
    }

    @GetMapping("/admin/edit-congty/{id}")
    public String adminShowEditForm(@PathVariable Long id, Model model) {
        try {
            CongTy congTy = congTyService.getCongTyById(id);
            model.addAttribute("congTy", congTy);
            return "admin/QLCongTy/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dscongty";
        }
    }

    @PostMapping("/admin/edit-congty/{id}")
    public String adminUpdateCongTy(@PathVariable Long id,
                             @ModelAttribute CongTy congTy,
                             RedirectAttributes redirectAttributes) {
        try {
            congTy.setId(id);
            congTyService.updateCongTy(congTy);
            redirectAttributes.addFlashAttribute("success", "Cập nhật công ty thành công!");
            return "redirect:/admin/dscongty";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/edit-congty/" + id;
        }
    }

    @GetMapping("/admin/delete-congty/{id}")
    public String adminDeleteCongTy(@PathVariable Long id, 
                              RedirectAttributes redirectAttributes) {
        try {
            congTyService.deleteCongTy(id);
            redirectAttributes.addFlashAttribute("success", "Xóa công ty thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dscongty";
    }

    @GetMapping("/chitiet-congty/{id}")
    public String showChiTietCongTy(@PathVariable Long id, Model model) {
        try {
            CongTy congTy = congTyService.getCongTyById(id);
            model.addAttribute("congTy", congTy);
            return "user/CongTy/ChiTiet";
        } catch (RuntimeException e) {
            return "redirect:/dscongty";
        }
    }    
    @GetMapping("/nhatd/edit-congty")
    public String nhaTDUpdateCongTy(Model model, Authentication authentication) {
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        try {
            CongTy congTy = congTyService.getCongTyForCurrentUser(userHienTai);
            model.addAttribute("congTy", congTy);
            model.addAttribute("user", userHienTai);
            return "nhatuyendung/CapNhatCongTy";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải thông tin công ty: " + e.getMessage());
            return "nhatuyendung/CapNhatCongTy";
        }
    }

    @PostMapping("/nhatd/edit-congty")
    public String nhaTDUpdateCongTy(
            @ModelAttribute("congTy") CongTy congTy,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            congTyService.updateThongTinCongTy(userHienTai, congTy, logoFile);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin công ty thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật công ty: " + e.getMessage());
        }
        
        return "redirect:/nhatd/edit-congty";
    }

    @PostMapping("/nhatd/edit-diachicongty")
    public String nhaTDUpdateDiaChiCongTy(
            @RequestParam("diaChi") String diaChi,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            congTyService.updateDiaChiCongTy(userHienTai, diaChi);
            redirectAttributes.addFlashAttribute("success", "Cập nhật địa chỉ công ty thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật địa chỉ: " + e.getMessage());
        }
        
        return "redirect:/nhatd/edit-congty";
    }

    @PostMapping("/nhatd/edit-mxhcongty")
    public String nhaTDUpdateMXHCongTy(
            @RequestParam("facebook") String facebook,
            @RequestParam("linkedIn") String linkedIn,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            congTyService.updateMXHCongTy(userHienTai, facebook, linkedIn);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin mạng xã hội thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật mạng xã hội: " + e.getMessage());
        }
        
        return "redirect:/nhatd/edit-congty";
    }
}
