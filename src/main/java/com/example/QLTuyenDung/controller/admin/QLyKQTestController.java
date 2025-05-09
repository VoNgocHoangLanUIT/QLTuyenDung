package com.example.QLTuyenDung.controller.admin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.KQBaiTest;
import com.example.QLTuyenDung.service.BaiTestService;
import com.example.QLTuyenDung.service.DonUngTuyenService;
import com.example.QLTuyenDung.service.KQBaiTestService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class QLyKQTestController {
    private final KQBaiTestService kqBaiTestService;
    private final DonUngTuyenService donUngTuyenService;
    private final BaiTestService baiTestService;

    @GetMapping("/dskqbaitest")
    public String showDSKQBaiTest(
            @RequestParam(required = false) Integer diemNgonNgu,
            @RequestParam(required = false) Integer diemLogic,
            @RequestParam(required = false) Integer diemChuyenMon,
            Model model) {

        // Check if any filter is applied
        boolean hasFilters = diemNgonNgu != null || diemLogic != null || diemChuyenMon != null;

        List<KQBaiTest> results;
        if (hasFilters) {
            // Apply filters if any parameter is provided
            List<DonUngTuyen> filteredDons = kqBaiTestService.filterKetQuaBaiTest(diemNgonNgu, diemLogic, diemChuyenMon);
            results = filteredDons.stream()
            .flatMap(don -> don.getDSKQBaiTest().stream())
            .collect(Collectors.toList());
        } else {
            // Show all results if no filters
            results = kqBaiTestService.getAllKQBaiTest();
        }

        Map<Long, List<KQBaiTest>> groupedResults = results.stream()
            .collect(Collectors.groupingBy(kq -> kq.getDonUngTuyen().getId()));

        model.addAttribute("groupedResults", groupedResults);
        return "admin/QLQuyTrinhTuyenDung/QLKQBaiTest/index";
    }

    @GetMapping("/add-kqbaitest")
    public String addKQBaiTest(Model model) {
        KQBaiTest kqBaiTest = new KQBaiTest();
        model.addAttribute("kqBaiTest", kqBaiTest);
        model.addAttribute("dSDonUT", donUngTuyenService.getAllDonUngTuyen());
        model.addAttribute("dsBaiTest", baiTestService.getAllBaiTest());
        return "admin/QLQuyTrinhTuyenDung/QLKQBaiTest/add";
    }

    @PostMapping("/add-kqbaitest")
    public String addKQBaiTest(@ModelAttribute KQBaiTest kqBaiTest,
                              RedirectAttributes redirectAttributes) {
        try {
            kqBaiTestService.addKQBaiTest(kqBaiTest);
            redirectAttributes.addFlashAttribute("success", "Thêm kết quả bài test thành công!");
            return "redirect:/admin/dskqbaitest";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/add-kqbaitest";
        }
    }

    @GetMapping("/edit-kqbaitest/{id}")
    public String updateKQBaiTest(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("kqBaiTest", kqBaiTestService.getKQBaiTestById(id));
            return "admin/QLQuyTrinhTuyenDung/QLKQBaiTest/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dskqbaitest";
        }
    }

    @PostMapping("/edit-kqbaitest/{id}")
    public String updateKQBaiTest(@PathVariable Long id,
                                 @ModelAttribute KQBaiTest kqBaiTest,
                                 RedirectAttributes redirectAttributes) {
        try {
            KQBaiTest existingKQ = kqBaiTestService.getKQBaiTestById(id);
            kqBaiTest.setId(id);
            kqBaiTest.setDonUngTuyen(existingKQ.getDonUngTuyen());
            kqBaiTest.setBaiTest(existingKQ.getBaiTest());
            kqBaiTest.setNgayLam(existingKQ.getNgayLam());
            kqBaiTestService.updateKQBaiTest(kqBaiTest);
            redirectAttributes.addFlashAttribute("success", "Cập nhật kết quả bài test thành công!");
            return "redirect:/admin/dskqbaitest";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/edit-kqbaitest/" + id;
        }
    }

    @GetMapping("/delete-kqbaitest/{id}")
    public String deleteKQBaiTest(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            kqBaiTestService.deleteKQBaiTest(id);
            redirectAttributes.addFlashAttribute("success", "Xóa kết quả bài test thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dskqbaitest";
    }

    @PostMapping("/duyetphongvan")
    public String duyetPhongVan(@RequestParam("selectedDons") List<Long> donIds,
                                    RedirectAttributes redirectAttributes) {
        try {
            kqBaiTestService.duyetPhongVan(donIds);
            redirectAttributes.addFlashAttribute("success", 
                "Đã duyệt " + donIds.size() + " đơn ứng tuyển cho phỏng vấn!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dskqbaitest";
    }
}
