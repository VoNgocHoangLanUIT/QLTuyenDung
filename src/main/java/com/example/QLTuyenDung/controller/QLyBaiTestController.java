package com.example.QLTuyenDung.controller;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.BaiTest;
import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.BaiTestService;
import com.example.QLTuyenDung.service.TinTuyenDungService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyBaiTestController {
    private final BaiTestService baiTestService;
    private final TinTuyenDungService tinTuyenDungService;

    @GetMapping("/admin/dsbaitest")
    public String adminShowDSBaiTest(Model model) {
        model.addAttribute("dSBaiTest", baiTestService.getAllBaiTest());
        return "admin/QLQuyTrinhTuyenDung/QLBaiTest/index";
    }

    @GetMapping("/admin/add-baitest")
    public String adminAddBaiTest(Model model) {
        BaiTest baiTest = new BaiTest();
        model.addAttribute("baiTest", baiTest);
        model.addAttribute("dsTinTD", tinTuyenDungService.getAllTinTuyenDung());
        return "admin/QLQuyTrinhTuyenDung/QLBaiTest/add";
    }

    @PostMapping("/admin/add-baitest")
    public String adminAddBaiTest(@ModelAttribute BaiTest baiTest,
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

    @GetMapping("/admin/edit-baitest/{id}")
    public String adminUpdateBaiTest(@PathVariable Long id, Model model) {
        try {
            BaiTest baiTest = baiTestService.getBaiTestById(id);
            model.addAttribute("baiTest", baiTest);
            model.addAttribute("dsTinTD", tinTuyenDungService.getAllTinTuyenDung());
            return "admin/QLQuyTrinhTuyenDung/QLBaiTest/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dsbaitest";
        }
    }

    @PostMapping("/admin/edit-baitest/{id}")
    public String adminUpdateBaiTest(@PathVariable Long id,
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

    @GetMapping("/admin/delete-baitest/{id}")
    public String adminDeleteBaiTest(@PathVariable Long id,
                              RedirectAttributes redirectAttributes) {
        try {
            baiTestService.deleteBaiTest(id);
            redirectAttributes.addFlashAttribute("success", "Xóa bài test thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dsbaitest";
    }

    @GetMapping("/nhatd/dsbaitest")
    public String nhaTDShowDSBaiTest(Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy danh sách bài test của công ty của nhà tuyển dụng (đã sắp xếp)
            List<BaiTest> dsBaiTest = baiTestService.getBaiTestByCongtyIdSortedByTinTD(userHienTai.getCongTy().getId());
            
            // Lấy danh sách bài test được nhóm theo tin tuyển dụng
            Map<TinTuyenDung, List<BaiTest>> groupedTests = 
                baiTestService.getBaiTestGroupedByTinTD(userHienTai.getCongTy().getId());
            
            // Tính số lượng bài test theo loại
            long ngonNguCount = dsBaiTest.stream().filter(b -> "ngonngu".equals(b.getLoai())).count();
            long logicCount = dsBaiTest.stream().filter(b -> "logic".equals(b.getLoai())).count();
            long chuyenMonCount = dsBaiTest.stream().filter(b -> "chuyenmon".equals(b.getLoai())).count();

            // Tính toán số lượng ứng viên không trùng cho mỗi tin tuyển dụng
            Map<TinTuyenDung, Integer> uniqueUngVienCounts = new HashMap<>();
            for (Map.Entry<TinTuyenDung, List<BaiTest>> entry : groupedTests.entrySet()) {
                int uniqueCount = (int) entry.getValue().stream()
                    .filter(baiTest -> baiTest.getDSKQBaiTest() != null && !baiTest.getDSKQBaiTest().isEmpty())
                    .flatMap(baiTest -> baiTest.getDSKQBaiTest().stream())
                    .map(kq -> kq.getDonUngTuyen().getId())
                    .distinct()
                    .count();
                uniqueUngVienCounts.put(entry.getKey(), uniqueCount);
            }
            

            model.addAttribute("dsBaiTest", dsBaiTest); // Giữ lại danh sách thông thường (đã sắp xếp)
            model.addAttribute("groupedTests", groupedTests); // Thêm danh sách đã nhóm
            model.addAttribute("ngonNguCount", ngonNguCount);
            model.addAttribute("logicCount", logicCount);
            model.addAttribute("chuyenMonCount", chuyenMonCount);
            model.addAttribute("totalCount", dsBaiTest.size());
            model.addAttribute("uniqueUngVienCounts", uniqueUngVienCounts); // Số lượng ứng viên không trùng theo tin tuyển dụng
            
            return "nhatuyendung/QLBaiTest/index";
        } catch (Exception e) {
            return "redirect:/nhatd";
        }
    }


    @GetMapping("/nhatd/add-baitest")
    public String nhaTDAddBaiTest(Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Tạo bài test mới và lấy danh sách tin tuyển dụng của công ty
            BaiTest baiTest = new BaiTest();
            baiTest.setNgayTao(new Date()); // Đặt ngày tạo là ngày hiện tại
            // Tính ngày đóng mặc định là 30 ngày sau
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.DATE, 30);
            baiTest.setNgayDong(c.getTime());
            
            // Lấy danh sách tin tuyển dụng của công ty
            List<TinTuyenDung> dsTinTD = tinTuyenDungService.getTinTuyenDungByCongTy(userHienTai.getCongTy());
            
            model.addAttribute("baiTest", baiTest);
            model.addAttribute("dsTinTD", dsTinTD);
            
            return "nhatuyendung/QLBaiTest/add";
        } catch (Exception e) {
            return "redirect:/nhatd/dsbaitest";
        }
    }

    @PostMapping("/nhatd/add-baitest")
    public String nhaTDAddBaiTest(@ModelAttribute BaiTest baiTest,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Kiểm tra tin tuyển dụng có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() != null && baiTest.getTinTuyenDung().getId() != null) {
                TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(baiTest.getTinTuyenDung().getId());
                if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                    redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thêm bài test cho tin tuyển dụng này!");
                    return "redirect:/nhatd/add-baitest";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn tin tuyển dụng!");
                return "redirect:/nhatd/add-baitest";
            }
            
            // Kiểm tra valid dữ liệu nhập vào
            if (baiTest.getTieuDe() == null || baiTest.getTieuDe().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tiêu đề bài test không được để trống!");
                return "redirect:/nhatd/add-baitest";
            }
            
            if (baiTest.getLoai() == null || baiTest.getLoai().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn loại bài test!");
                return "redirect:/nhatd/add-baitest";
            }
            
            if (baiTest.getLinkGGForm() == null || baiTest.getLinkGGForm().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Link bài test không được để trống!");
                return "redirect:/nhatd/add-baitest";
            }
            
            if (baiTest.getNgayTao() == null) {
                baiTest.setNgayTao(new Date());
            }
            
            if (baiTest.getNgayDong() == null) {
                // Ngày đóng mặc định là 30 ngày sau ngày tạo
                Calendar c = Calendar.getInstance();
                c.setTime(baiTest.getNgayTao());
                c.add(Calendar.DATE, 30);
                baiTest.setNgayDong(c.getTime());
            }
            
            // Kiểm tra ngày đóng phải sau ngày tạo
            if (baiTest.getNgayDong().before(baiTest.getNgayTao())) {
                redirectAttributes.addFlashAttribute("error", "Ngày đóng phải sau ngày tạo bài test!");
                return "redirect:/nhatd/add-baitest";
            }
            
            // Lưu bài test
            baiTestService.addBaiTest(baiTest);
            redirectAttributes.addFlashAttribute("success", "Thêm bài test thành công!");
            return "redirect:/nhatd/dsbaitest";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/add-baitest";
        }
    }

    @GetMapping("/nhatd/chitiet-baitest/{id}")
    public String nhaTDChiTietBaiTest(@PathVariable Long id, 
                                    Model model, 
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy bài test cần xem
            BaiTest baiTest = baiTestService.getBaiTestById(id);
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() == null || 
                !baiTest.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem bài test này!");
                return "redirect:/nhatd/dsbaitest";
            }
            
            model.addAttribute("baiTest", baiTest);
            
            return "nhatuyendung/QLBaiTest/ChiTiet";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/dsbaitest";
        }
    }

    @GetMapping("/nhatd/edit-baitest/{id}")
    public String nhaTDUpdateBaiTest(@PathVariable Long id, 
                                Model model, 
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy bài test cần chỉnh sửa
            BaiTest baiTest = baiTestService.getBaiTestById(id);
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() == null || 
                !baiTest.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa bài test này!");
                return "redirect:/nhatd/dsbaitest";
            }
            
            // Lấy danh sách tin tuyển dụng của công ty
            List<TinTuyenDung> dsTinTD = tinTuyenDungService.getTinTuyenDungByCongTy(userHienTai.getCongTy());
            
            model.addAttribute("baiTest", baiTest);
            model.addAttribute("dsTinTD", dsTinTD);
            
            return "nhatuyendung/QLBaiTest/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/dsbaitest";
        }
    }

    @PostMapping("/nhatd/edit-baitest/{id}")
    public String nhaTDEditBaiTestSubmit(@PathVariable Long id,
                                        @ModelAttribute BaiTest baiTest,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy bài test hiện tại từ cơ sở dữ liệu
            BaiTest baiTestHienTai = baiTestService.getBaiTestById(id);
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (baiTestHienTai.getTinTuyenDung() == null || 
                !baiTestHienTai.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa bài test này!");
                return "redirect:/nhatd/dsbaitest";
            }
            
            // Kiểm tra tin tuyển dụng mới có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() != null && baiTest.getTinTuyenDung().getId() != null) {
                TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(baiTest.getTinTuyenDung().getId());
                if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                    redirectAttributes.addFlashAttribute("error", "Bạn không có quyền gán bài test cho tin tuyển dụng này!");
                    return "redirect:/nhatd/edit-baitest/" + id;
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn tin tuyển dụng!");
                return "redirect:/nhatd/edit-baitest/" + id;
            }
            
            // Kiểm tra valid dữ liệu nhập vào
            if (baiTest.getTieuDe() == null || baiTest.getTieuDe().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tiêu đề bài test không được để trống!");
                return "redirect:/nhatd/edit-baitest/" + id;
            }
            
            if (baiTest.getLoai() == null || baiTest.getLoai().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn loại bài test!");
                return "redirect:/nhatd/edit-baitest/" + id;
            }
            
            if (baiTest.getLinkGGForm() == null || baiTest.getLinkGGForm().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Link bài test không được để trống!");
                return "redirect:/nhatd/edit-baitest/" + id;
            }
            
            // Giữ ngày tạo ban đầu
            baiTest.setNgayTao(baiTestHienTai.getNgayTao());
            
            if (baiTest.getNgayDong() == null) {
                // Ngày đóng mặc định là 30 ngày sau ngày tạo
                Calendar c = Calendar.getInstance();
                c.setTime(baiTest.getNgayTao());
                c.add(Calendar.DATE, 30);
                baiTest.setNgayDong(c.getTime());
            }
            
            // Kiểm tra ngày đóng phải sau ngày tạo
            if (baiTest.getNgayDong().before(baiTest.getNgayTao())) {
                redirectAttributes.addFlashAttribute("error", "Ngày đóng phải sau ngày tạo bài test!");
                return "redirect:/nhatd/edit-baitest/" + id;
            }
            
            // Đảm bảo giữ ID đúng
            baiTest.setId(id);
            
            // Cập nhật bài test
            baiTestService.updateBaiTest(baiTest);
            redirectAttributes.addFlashAttribute("success", "Cập nhật bài test thành công!");
            return "redirect:/nhatd/chitiet-baitest/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/edit-baitest/" + id;
        }
    }

    @GetMapping("/nhatd/delete-baitest/{id}")
    public String nhaTDDeleteBaiTest(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy bài test cần xóa
            BaiTest baiTest = baiTestService.getBaiTestById(id);
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() == null || 
                !baiTest.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa bài test này!");
                return "redirect:/nhatd/dsbaitest";
            }
            
            // Xóa bài test
            baiTestService.deleteBaiTest(id);
            redirectAttributes.addFlashAttribute("success", "Xóa bài test thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/nhatd/dsbaitest";
    }
}
