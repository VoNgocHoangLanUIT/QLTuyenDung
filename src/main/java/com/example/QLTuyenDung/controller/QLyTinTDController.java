package com.example.QLTuyenDung.controller;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.TinYeuThich;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.CongTyService;
import com.example.QLTuyenDung.service.TinTuyenDungService;
import com.example.QLTuyenDung.service.TinYeuThichService;
import com.example.QLTuyenDung.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyTinTDController {
    private final TinTuyenDungService tinTuyenDungService;
    private final CongTyService congTyService;
    private final UserService userService;
    private final TinYeuThichService tinYeuThichService;

    @GetMapping("/admin/dstintd")
    public String adminShowDSTinTD(Model model) {
        List<TinTuyenDung> dSTinTD = tinTuyenDungService.getAllTinTuyenDung();
        model.addAttribute("dSTinTD", dSTinTD);
        return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/index";
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

    @GetMapping("/dstintd")
    public String userShowDSTinTD(Model model, 
                                Authentication authentication,
                                HttpServletRequest request,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(defaultValue = "ngayDang") String sort,
                                @RequestParam(defaultValue = "desc") String direction) {
        
        // Lấy tin tuyển dụng có phân trang
        Page<TinTuyenDung> pageTinTD = tinTuyenDungService.getTinTuyenDungByTrangThaiPaginated("dangtuyen", page, size, sort, direction);
        List<TinTuyenDung> dSTinTD = pageTinTD.getContent();
        
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
        
        // Thêm thông tin phân trang vào model
        model.addAttribute("dSTinTD", dSTinTD);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageTinTD.getTotalPages());
        model.addAttribute("totalItems", pageTinTD.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("reverseSortDir", direction.equals("asc") ? "desc" : "asc");
        
        // Thông tin cho phân trang
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(pageTinTD.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("request", request);
        
        return "user/TinTuyenDung/index";
    }

    // Tương tự cập nhật cho phương thức tìm kiếm
    @GetMapping("/tim-kiem")
    public String timKiemTinTuyenDung(
        @RequestParam(required = false) String tuKhoa, 
        @RequestParam(required = false) String diaDiem,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "ngayDang") String sort,
        @RequestParam(defaultValue = "desc") String direction,
        Model model,
        HttpServletRequest request, 
        Authentication authentication) {
        
        // Tìm kiếm với phân trang
        Page<TinTuyenDung> pageTinTD = tinTuyenDungService.timKiemTinTuyenDungPaginated(tuKhoa, diaDiem, page, size, sort, direction);
        List<TinTuyenDung> ketQuaTimKiem = pageTinTD.getContent();
        
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
        
        // Thêm thông tin phân trang vào model
        model.addAttribute("dSTinTD", ketQuaTimKiem);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageTinTD.getTotalPages());
        model.addAttribute("totalItems", pageTinTD.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("direction", direction);
        model.addAttribute("reverseSortDir", direction.equals("asc") ? "desc" : "asc");
        
        // Thông tin cho phân trang
        int startPage = Math.max(0, page - 2);
        int endPage = Math.min(pageTinTD.getTotalPages() - 1, page + 2);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        
        // Truyền dữ liệu tìm kiếm vào view
        model.addAttribute("tuKhoa", tuKhoa);
        model.addAttribute("diaDiem", diaDiem);
        model.addAttribute("request", request);
        
        return "user/TinTuyenDung/index";
    }

    @GetMapping("/chitiet-tintd/{id}")
    public String chiTietTinTuyenDung(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(id);
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            
            // Kiểm tra xem tin đã được bookmark chưa
            if (authentication != null && authentication.isAuthenticated()) {
                CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
                User user = userDetails.getUser();
                
                // Kiểm tra quyền
                boolean isCandidate = userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("CANDIDATE"));
                    
                if (isCandidate) {
                    boolean isBookmarked = tinYeuThichService.isBookmarked(user.getId(), id);
                    model.addAttribute("isBookmarked", isBookmarked);
                }
            }
            
            return "user/TinTuyenDung/ChiTiet";
        } catch (Exception e) {
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
    public String nhaTDShowDSTinTuyenDung(Model model, Authentication authentication) {
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

    @GetMapping("/nvhs/dstintd")
    public String nVHSshowDSTinTuyenDung(Model model, Authentication authentication) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nvhs";
            }
            
            // Lấy danh sách tin tuyển dụng theo công ty của nhà tuyển dụng
            List<TinTuyenDung> dSTinTD = tinTuyenDungService.getTinTuyenDungByCongTy(userHienTai.getCongTy());
            model.addAttribute("dSTinTD", dSTinTD);
            
            return "nvhs/QLTinTuyenDung/index";
        } catch (Exception e) {
            return "redirect:/nvhs";
        }
    }

    
}
