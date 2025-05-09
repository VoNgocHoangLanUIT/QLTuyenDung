package com.example.QLTuyenDung.controller.admin;

import java.util.List;

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
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.service.CongTyService;
import com.example.QLTuyenDung.service.TinTuyenDungService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class QLyTinTDController {
    private final TinTuyenDungService tinTuyenDungService;
    private final CongTyService congTyService;

    @GetMapping("/dstintd")
    public String showDSTinTD(Model model) {
        List<TinTuyenDung> dSTinTD = tinTuyenDungService.getAllTinTuyenDung();
        model.addAttribute("dSTinTD", dSTinTD);
        return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/index";
    }

    @GetMapping("/add-tintd")
    public String addTinTuyenDung(Model model) {
        TinTuyenDung tinTuyenDung = new TinTuyenDung();
        tinTuyenDung.setTrangThai("dangtuyen");
        model.addAttribute("tinTuyenDung", tinTuyenDung);
        List<CongTy> dsCongTy = congTyService.getAllCongTy();
        model.addAttribute("dsCongTy", dsCongTy);
        return "admin/QLQuyTrinhTuyenDung/QLTinTuyenDung/add";
    }

    @PostMapping("/add-tintd")
    public String addTinTuyenDung(@ModelAttribute TinTuyenDung tinTuyenDung,
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

    @GetMapping("/edit-tintd/{id}")
    public String updateTinTuyenDung(@PathVariable Long id, Model model) {
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

    @PostMapping("/edit-tintd/{id}")
    public String updateTinTuyenDung(@PathVariable Long id,
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

    @GetMapping("/delete-tintd/{id}")
    public String deleteTinTuyenDung(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tinTuyenDungService.deleteTinTuyenDung(id);
            redirectAttributes.addFlashAttribute("success", "Xóa tin tuyển dụng thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dstintd";
    }
}
