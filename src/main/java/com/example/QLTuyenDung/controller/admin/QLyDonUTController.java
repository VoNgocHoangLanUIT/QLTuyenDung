package com.example.QLTuyenDung.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.service.DonUngTuyenService;
import com.example.QLTuyenDung.service.TinTuyenDungService;
import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class QLyDonUTController {
    private final DonUngTuyenService donUngTuyenService;
    private final UserService userService;
    private final TinTuyenDungService tinTuyenDungService;

    @GetMapping("/dsdonut")
    public String showDSDonUT(Model model) {
        model.addAttribute("dSDonUT", donUngTuyenService.getAllDonUngTuyen());
        return "admin/QLQuyTrinhTuyenDung/QLDonUngTuyen/index";
    }

    @GetMapping("/add-donut")
    public String addDonUT(Model model) {
        DonUngTuyen donUngTuyen = new DonUngTuyen();

        model.addAttribute("donUngTuyen", donUngTuyen);
        model.addAttribute("dsUser", userService.getAllCandidates()); 
        model.addAttribute("dsTinTD", tinTuyenDungService.getAllTinTuyenDung());
        
        return "admin/QLQuyTrinhTuyenDung/QLDonUngTuyen/add";
    }

    @PostMapping("/add-donut")
    public String addDonUT(@ModelAttribute DonUngTuyen donUngTuyen,
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

    @GetMapping("/edit-donut/{id}")
    public String updateDonUT(@PathVariable Long id, Model model) {
        try {
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(id);
            model.addAttribute("donUngTuyen", donUngTuyen);
            return "admin/QLQuyTrinhTuyenDung/QLDonUngTuyen/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dsdonut";
        }
    }

    @PostMapping("/edit-donut/{id}")
    public String updateDonUT(@PathVariable Long id, 
                            @ModelAttribute DonUngTuyen donUngTuyen,
                            RedirectAttributes redirectAttributes) {
        try {
            DonUngTuyen existingDon = donUngTuyenService.getDonUngTuyenById(id);
            donUngTuyen.setId(id);
            donUngTuyen.setNgayUngTuyen(existingDon.getNgayUngTuyen());
            donUngTuyenService.updateDonUngTuyen(donUngTuyen);
            redirectAttributes.addFlashAttribute("success", "Cập nhật đơn ứng tuyển thành công!");
            return "redirect:/admin/dsdonut";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/edit-donut/" + id;
        }
    }

    @GetMapping("/delete-donut/{id}")
    public String deleteDonUT(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            donUngTuyenService.deleteDonUngTuyen(id);
            redirectAttributes.addFlashAttribute("success", "Xóa đơn ứng tuyển thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dsdonut";
    }
}
