package com.example.QLTuyenDung.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.CongTyService;
import com.example.QLTuyenDung.service.TinTuyenDungService;
import com.example.QLTuyenDung.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyTinTDController {
    private final TinTuyenDungService tinTuyenDungService;
    private final CongTyService congTyService;
    private final UserService userService;

    @GetMapping("/admin/dstintd")
    public String adminShowDSTinTD(Model model) {
        List<TinTuyenDung> dSTinTD = tinTuyenDungService.getAllTinTuyenDung();
        model.addAttribute("dSTinTD", dSTinTD);
        return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/index";
    }

    @GetMapping("/dstintd")
    public String userShowDSTinTD(Model model) {
        List<TinTuyenDung> dSTinTD = tinTuyenDungService.getAllTinTuyenDung();
        model.addAttribute("dSTinTD", dSTinTD);
        return "user/TinTuyenDung/index";
    }

    @GetMapping("/admin/add-tintd")
    public String adminAddTinTuyenDung(Model model) {
        TinTuyenDung tinTuyenDung = new TinTuyenDung();
        tinTuyenDung.setTrangThai("dangtuyen");
        model.addAttribute("tinTuyenDung", tinTuyenDung);
        List<CongTy> dsCongTy = congTyService.getAllCongTy();
        model.addAttribute("dsCongTy", dsCongTy);
        return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/add";
    }

    @PostMapping("/admin/add-tintd")
    public String adminAddTinTuyenDung(@ModelAttribute TinTuyenDung tinTuyenDung,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (tinTuyenDung.getCongty() == null || tinTuyenDung.getCongty().getId() == null) {
            bindingResult.rejectValue("congty", "error.tinTuyenDung", "Vui lòng chọn công ty");
        }
                                
        if (bindingResult.hasErrors()) {
            model.addAttribute("dsCongTy", congTyService.getAllCongTy());
            return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/add";
        }
        try {
            tinTuyenDungService.addTinTuyenDung(tinTuyenDung);
            redirectAttributes.addFlashAttribute("success", "Đăng tin tuyển dụng thành công!");
            return "redirect:/admin/dstintd";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/add-tintd";
        }
    }

    @GetMapping("/admin/edit-tintd/{id}")
    public String adminUpdateTinTuyenDung(@PathVariable Long id, Model model) {
        try {
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(id);
            List<CongTy> dsCongTy = congTyService.getAllCongTy();
            
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            model.addAttribute("dsCongTy", dsCongTy);
            
            return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dstintd";
        }
    }

    @PostMapping("/admin/edit-tintd/{id}")
    public String adminUpdateTinTuyenDung(@PathVariable Long id,
                                @ModelAttribute TinTuyenDung tinTuyenDung,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (tinTuyenDung.getCongty() == null || tinTuyenDung.getCongty().getId() == null) {
            bindingResult.rejectValue("congty", "error.tinTuyenDung", "Vui lòng chọn công ty");
            return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/edit";
        }

        try {
            tinTuyenDung.setId(id);
            tinTuyenDungService.updateTinTuyenDung(tinTuyenDung);
            redirectAttributes.addFlashAttribute("success", "Cập nhật tin tuyển dụng thành công!");
            return "redirect:/admin/dstintd";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/edit-tintd/" + id;
        }
    }

    @GetMapping("/admin/delete-tintd/{id}")
    public String adminDeleteTinTuyenDung(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tinTuyenDungService.deleteTinTuyenDung(id);
            redirectAttributes.addFlashAttribute("success", "Xóa tin tuyển dụng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dstintd";
    }

    @GetMapping("/chitiet-tintd/{id}")
    public String showChiTietTinTuyenDung(@PathVariable Long id, Model model) {
        try {
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(id);
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            return "user/TinTuyenDung/ChiTiet";
        } catch (RuntimeException e) {
            return "redirect:/dstintd";
        }
    }

    @GetMapping("/nhatd/add-tintd")
    public String nhaTDAddTinTuyenDung(Model model, Authentication authentication) {
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        // Kiểm tra xem user đã có công ty chưa
        if (userHienTai.getCongTy() == null) {
            return "redirect:/nhatd/edit-congty";
        }
        
        TinTuyenDung tinTuyenDung = new TinTuyenDung();
        tinTuyenDung.setCongty(userHienTai.getCongTy());
        model.addAttribute("tinTuyenDung", tinTuyenDung);
        
        return "nhatuyendung/QLTinTuyenDung/DangTinTD";
    }

    @PostMapping("/nhatd/add-tintd")
    public String nhaTDAddTinTuyenDung(@Valid @ModelAttribute TinTuyenDung tinTuyenDung, 
                         BindingResult bindingResult,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "nhatuyendung/QLTinTuyenDung/DangTinTD";
        }
        
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Đảm bảo tin được đăng cho công ty của người dùng hiện tại
            tinTuyenDung.setCongty(userHienTai.getCongTy());
            
            tinTuyenDungService.addTinTuyenDung(tinTuyenDung);
            redirectAttributes.addFlashAttribute("success", "Đăng tin tuyển dụng thành công!");
            return "redirect:/nhatd/dstintd";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/dang-tin";
        }
    }

    @GetMapping("/nhatd/dstintd")
    public String showDSTinTuyenDung(Model model, Authentication authentication) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy danh sách tin tuyển dụng theo công ty của nhà tuyển dụng
            List<TinTuyenDung> dSTinTD = tinTuyenDungService.getTinTuyenDungByCongTy(userHienTai.getCongTy());
            model.addAttribute("dSTinTD", dSTinTD);
            
            return "nhatuyendung/QLTinTuyenDung/index";
        } catch (Exception e) {
            return "redirect:/nhatd";
        }
    }

    @GetMapping("/nhatd/edit-tintd/{id}")
    public String nhaTDUpdateTinTuyenDung(@PathVariable Long id, 
                                    Model model, 
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(id);
            
            // Kiểm tra xem tin này có thuộc công ty của nhà tuyển dụng không
            if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa tin tuyển dụng này!");
                return "redirect:/nhatd/dstintd";
            }
            
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            return "nhatuyendung/QLTinTuyenDung/edit";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/dstintd";
        }
    }

    @PostMapping("/nhatd/edit-tintd/{id}")
    public String nhaTDUpdateTinTuyenDung(@PathVariable Long id,
                                        @Valid @ModelAttribute TinTuyenDung tinTuyenDung,
                                        BindingResult bindingResult,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "nhatuyendung/QLTinTuyenDung/edit";
        }
        
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Lấy tin tuyển dụng hiện tại từ cơ sở dữ liệu
            TinTuyenDung tinHienTai = tinTuyenDungService.getTinTuyenDungById(id);
            
            // Kiểm tra xem tin này có thuộc công ty của nhà tuyển dụng không
            if (!tinHienTai.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa tin tuyển dụng này!");
                return "redirect:/nhatd/dstintd";
            }
            
            // Giữ các giá trị không thay đổi
            tinTuyenDung.setId(id);
            tinTuyenDung.setCongty(tinHienTai.getCongty());
            tinTuyenDung.setNgayDang(tinHienTai.getNgayDang());
            
            // Cập nhật tin tuyển dụng
            tinTuyenDungService.updateTinTuyenDung(tinTuyenDung);
            redirectAttributes.addFlashAttribute("success", "Cập nhật tin tuyển dụng thành công!");
            return "redirect:/nhatd/dstintd";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/edit-tintd/" + id;
        }
    }

    @GetMapping("/nhatd/delete-tintd/{id}")
    public String nhaTDDeleteTinTuyenDung(@PathVariable Long id, 
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(id);
            
            // Kiểm tra xem tin này có thuộc công ty của nhà tuyển dụng không
            if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa tin tuyển dụng này!");
                return "redirect:/nhatd/dstintd";
            }
            
            tinTuyenDungService.deleteTinTuyenDung(id);
            redirectAttributes.addFlashAttribute("success", "Xóa tin tuyển dụng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/nhatd/dstintd";
    }
}
