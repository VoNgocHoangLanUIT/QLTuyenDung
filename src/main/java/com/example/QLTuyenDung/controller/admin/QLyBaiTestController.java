package com.example.QLTuyenDung.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.BaiTest;
import com.example.QLTuyenDung.service.BaiTestService;
import com.example.QLTuyenDung.service.TinTuyenDungService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class QLyBaiTestController {
    private final BaiTestService baiTestService;
    private final TinTuyenDungService tinTuyenDungService;

    @GetMapping("/dsbaitest")
    public String showDSBaiTest(Model model) {
        model.addAttribute("dSBaiTest", baiTestService.getAllBaiTest());
        return "admin/QLQuyTrinhTuyenDung/QLBaiTest/index";
    }

    @GetMapping("/add-baitest")
    public String addBaiTest(Model model) {
        BaiTest baiTest = new BaiTest();
        model.addAttribute("baiTest", baiTest);
        model.addAttribute("dsTinTD", tinTuyenDungService.getAllTinTuyenDung());
        return "admin/QLQuyTrinhTuyenDung/QLBaiTest/add";
    }

    @PostMapping("/add-baitest")
    public String addBaiTest(@ModelAttribute BaiTest baiTest,
                           RedirectAttributes redirectAttributes) {
        try {
            baiTestService.addBaiTest(baiTest);
            redirectAttributes.addFlashAttribute("success", "Thêm bài test thành công!");
            return "redirect:/admin/dsbaitest";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/add-baitest";
        }
    }

    @GetMapping("/edit-baitest/{id}")
    public String updateBaiTest(@PathVariable Long id, Model model) {
        try {
            BaiTest baiTest = baiTestService.getBaiTestById(id);
            model.addAttribute("baiTest", baiTest);
            model.addAttribute("dsTinTD", tinTuyenDungService.getAllTinTuyenDung());
            return "admin/QLQuyTrinhTuyenDung/QLBaiTest/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dsbaitest";
        }
    }

    @PostMapping("/edit-baitest/{id}")
    public String updateBaiTest(@PathVariable Long id,
                              @ModelAttribute BaiTest baiTest,
                              RedirectAttributes redirectAttributes) {
        try {
            baiTest.setId(id);
            baiTestService.updateBaiTest(baiTest);
            redirectAttributes.addFlashAttribute("success", "Cập nhật bài test thành công!");
            return "redirect:/admin/dsbaitest";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/edit-baitest/" + id;
        }
    }

    @GetMapping("/delete-baitest/{id}")
    public String deleteBaiTest(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            baiTestService.deleteBaiTest(id);
            redirectAttributes.addFlashAttribute("success", "Xóa bài test thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dsbaitest";
    }
}
