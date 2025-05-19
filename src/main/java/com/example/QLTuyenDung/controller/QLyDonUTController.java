package com.example.QLTuyenDung.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.DonUngTuyenService;
import com.example.QLTuyenDung.service.PhongVanService;
import com.example.QLTuyenDung.service.TinTuyenDungService;
import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyDonUTController {
    private final DonUngTuyenService donUngTuyenService;
    private final UserService userService;
    private final TinTuyenDungService tinTuyenDungService;
    private final PhongVanService phongVanService;

    @GetMapping("/admin/dsdonut")
    public String adminShowDSDonUT(Model model) {
        model.addAttribute("dSDonUT", donUngTuyenService.getAllDonUngTuyen());
        return "admin/QLQuyTrinhTuyenDung/QLDonUngTuyen/index";
    }

    @GetMapping("/admin/add-donut")
    public String adminAddDonUT(Model model) {
        DonUngTuyen donUngTuyen = new DonUngTuyen();

        model.addAttribute("donUngTuyen", donUngTuyen);
        model.addAttribute("dsUser", userService.getAllCandidates()); 
        model.addAttribute("dsTinTD", tinTuyenDungService.getAllTinTuyenDung());
        
        return "admin/QLQuyTrinhTuyenDung/QLDonUngTuyen/add";
    }

    @PostMapping("/admin/add-donut")
    public String adminAddDonUT(@ModelAttribute DonUngTuyen donUngTuyen,
                          RedirectAttributes redirectAttributes) {
        try {
            donUngTuyenService.addDonUngTuyen(donUngTuyen);
            redirectAttributes.addFlashAttribute("success", "Thêm đơn ứng tuyển thành công!");
            return "redirect:/admin/dsdonut";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/add-donut";
        }
    }

    @GetMapping("/admin/edit-donut/{id}")
    public String adminUpdateDonUT(@PathVariable Long id, Model model) {
        try {
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(id);
            model.addAttribute("donUngTuyen", donUngTuyen);
            return "admin/QLQuyTrinhTuyenDung/QLDonUngTuyen/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dsdonut";
        }
    }

    @PostMapping("/admin/edit-donut/{id}")
    public String adminUpdateDonUT(@PathVariable Long id, 
                            @ModelAttribute DonUngTuyen donUngTuyen,
                            RedirectAttributes redirectAttributes) {
        try {
            DonUngTuyen existingDon = donUngTuyenService.getDonUngTuyenById(id);
            donUngTuyen.setId(id);
            donUngTuyen.setNgayUngTuyen(existingDon.getNgayUngTuyen());
            if(donUngTuyenService.kiemTraTrangThaiPV(donUngTuyen)){
                redirectAttributes.addFlashAttribute("info", 
                        "Đã tự động tạo phỏng vấn mới cho ứng viên này. Vui lòng phân công nhân viên phỏng vấn.");
            }
            donUngTuyenService.updateDonUngTuyen(donUngTuyen);
            redirectAttributes.addFlashAttribute("success", "Cập nhật đơn ứng tuyển thành công!");
            return "redirect:/admin/dsdonut";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/edit-donut/" + id;
        }
    }

    @GetMapping("/admin/delete-donut/{id}")
    public String adminDeleteDonUT(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            donUngTuyenService.deleteDonUngTuyen(id);
            redirectAttributes.addFlashAttribute("success", "Xóa đơn ứng tuyển thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dsdonut";
    }

    @GetMapping("/nhatd/dsdonut/{tinTdId}")
    public String nhaTDShowDSDonUTTheoTinTD(@PathVariable Long tinTdId, Model model, Authentication authentication) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin tin tuyển dụng
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(tinTdId);
            
            // Kiểm tra tin tuyển dụng có thuộc công ty của user không
            if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                return "redirect:/nhatd/dstintd";
            }
            
            // Lấy danh sách đơn ứng tuyển theo tin tuyển dụng
            List<DonUngTuyen> danhSachDonUT = donUngTuyenService.getDonUngTuyenByTinTuyenDungId(tinTdId);
            
            // Tính toán số lượng theo từng trạng thái
            long totalApplications = danhSachDonUT.size();
            long dangDuyetCount = danhSachDonUT.stream().filter(d -> "dangduyet".equals(d.getTrangThai())).count();
            long choBaiTestCount = danhSachDonUT.stream().filter(d -> "chotest".equals(d.getTrangThai())).count();
            long phongVanCount = danhSachDonUT.stream().filter(d -> "phongvan".equals(d.getTrangThai())).count();
            long daTuyenCount = danhSachDonUT.stream().filter(d -> "datuyen".equals(d.getTrangThai())).count();
            long tuChoiCount = danhSachDonUT.stream().filter(d -> "tuchoi".equals(d.getTrangThai())).count();
            
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            model.addAttribute("dsDonUT", danhSachDonUT);
            model.addAttribute("totalCount", totalApplications);
            model.addAttribute("dangDuyetCount", dangDuyetCount);
            model.addAttribute("choBaiTestCount", choBaiTestCount);
            model.addAttribute("phongVanCount", phongVanCount);
            model.addAttribute("daTuyenCount", daTuyenCount);
            model.addAttribute("tuChoiCount", tuChoiCount);
            
            return "nhatuyendung/QLDonUngTuyen/index";
        } catch (Exception e) {
            return "redirect:/nhatd/dstintd";
        }
    }

    @GetMapping("/nhatd/update-trangthai/{id}/{trangThai}")
    public String updateTrangThaiDonUT(@PathVariable Long id, 
                                    @PathVariable String trangThai,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin đơn ứng tuyển
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(id);
            
            // Kiểm tra đơn có thuộc về tin tuyển dụng của công ty user không
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền cập nhật đơn ứng tuyển này!");
                return "redirect:/nhatd/dstintd";
            }
            
            // Kiểm tra trạng thái hợp lệ
            if (!trangThai.equals("dangduyet") && !trangThai.equals("chotest") && !trangThai.equals("phongvan") 
                && !trangThai.equals("datuyen") && !trangThai.equals("tuchoi")) {
                redirectAttributes.addFlashAttribute("error", "Trạng thái không hợp lệ!");
                return "redirect:/nhatd/dsdonut/" + donUngTuyen.getTinTuyenDung().getId();
            }
            
            // Lưu trạng thái cũ để thông báo
            String statusMessage = donUngTuyenService.getStatusMessage(trangThai);
            
            // Cập nhật trạng thái
            donUngTuyen.setTrangThai(trangThai);
            if(donUngTuyenService.kiemTraTrangThaiPV(donUngTuyen)){
                redirectAttributes.addFlashAttribute("info", 
                        "Đã tự động tạo phỏng vấn mới cho ứng viên này. Vui lòng phân công nhân viên phỏng vấn.");
            }
            donUngTuyenService.updateDonUngTuyen(donUngTuyen);
            
            redirectAttributes.addFlashAttribute("success", "Đã chuyển trạng thái đơn ứng tuyển sang " + statusMessage);
            return "redirect:/nhatd/dsdonut/" + donUngTuyen.getTinTuyenDung().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return "redirect:/nhatd/dstintd";
        }
    }

}
