package com.example.QLTuyenDung.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.service.CongTyService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class QLyCongTyController {
    private final CongTyService congTyService;

    @GetMapping("/dscongty")
    public String showDSCongTy(Model model) {
        List<CongTy> dsCongTy = congTyService.getAllCongTy();
        model.addAttribute("dsCongTy", dsCongTy);
        return "admin/QLCongTy/index";
    }

    @GetMapping("/add-congty")
    public String addCongTy(Model model) {
        model.addAttribute("congTy", new CongTy());
        return "admin/QLCongTy/add";
    }

    @PostMapping("/add-congty")
    public String addCongTy(@ModelAttribute CongTy congTy,
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

    @GetMapping("/edit-congty/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            CongTy congTy = congTyService.getCongTyById(id);
            model.addAttribute("congTy", congTy);
            return "admin/QLCongTy/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dscongty";
        }
    }

    @PostMapping("/edit-congty/{id}")
    public String updateCongTy(@PathVariable Long id,
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

    @GetMapping("/delete-congty/{id}")
    public String deleteCongTy(@PathVariable Long id, 
                              RedirectAttributes redirectAttributes) {
        try {
            congTyService.deleteCongTy(id);
            redirectAttributes.addFlashAttribute("success", "Xóa công ty thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dscongty";
    }
}
