package com.example.QLTuyenDung.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.dto.ScoreDTO;
import com.example.QLTuyenDung.model.BaiTest;
import com.example.QLTuyenDung.model.CustomUserDetail;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.KQBaiTest;
import com.example.QLTuyenDung.model.TinTuyenDung;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.BaiTestService;
import com.example.QLTuyenDung.service.DiemService;
import com.example.QLTuyenDung.service.DonUngTuyenService;
import com.example.QLTuyenDung.service.KQBaiTestService;
import com.example.QLTuyenDung.service.TinTuyenDungService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class QLyKQTestController {
    private final KQBaiTestService kqBaiTestService;
    private final DonUngTuyenService donUngTuyenService;
    private final BaiTestService baiTestService;
    private final TinTuyenDungService tinTuyenDungService;
    private final DiemService diemService;

    @GetMapping("/admin/dskqbaitest")
    public String adminShowDSKQBaiTest(
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

    @GetMapping("/admin/add-kqbaitest")
    public String adminAddKQBaiTest(Model model) {
        KQBaiTest kqBaiTest = new KQBaiTest();
        model.addAttribute("kqBaiTest", kqBaiTest);
        model.addAttribute("dSDonUT", donUngTuyenService.getAllDonUngTuyen());
        model.addAttribute("dsBaiTest", baiTestService.getAllBaiTest());
        return "admin/QLQuyTrinhTuyenDung/QLKQBaiTest/add";
    }

    @PostMapping("/admin/add-kqbaitest")
    public String adminAddKQBaiTest(@ModelAttribute KQBaiTest kqBaiTest,
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

    @GetMapping("/admin/edit-kqbaitest/{id}")
    public String adminUpdateKQBaiTest(@PathVariable Long id, Model model) {
        try {
            model.addAttribute("kqBaiTest", kqBaiTestService.getKQBaiTestById(id));
            return "admin/QLQuyTrinhTuyenDung/QLKQBaiTest/edit";
        } catch (RuntimeException e) {
            return "redirect:/admin/dskqbaitest";
        }
    }

    @PostMapping("/admin/edit-kqbaitest/{id}")
    public String adminUpdateKQBaiTest(@PathVariable Long id,
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

    @GetMapping("/admin/delete-kqbaitest/{id}")
    public String adminDeleteKQBaiTest(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        try {
            kqBaiTestService.deleteKQBaiTest(id);
            redirectAttributes.addFlashAttribute("success", "Xóa kết quả bài test thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/dskqbaitest";
    }

    @PostMapping("/admin/duyetphongvan")
    public String adminDuyetPhongVan(@RequestParam("selectedDons") List<Long> donIds,
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

    @GetMapping("/nhatd/ds-truycap/{baiTestId}")
    public String nhaTDShowDSTruyCap(@PathVariable Long baiTestId, Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin bài test
            BaiTest baiTest = baiTestService.getBaiTestById(baiTestId);
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() == null || 
                !baiTest.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                return "redirect:/nhatd/dsbaitest";
            }
            
            // Lấy danh sách kết quả bài test
            List<KQBaiTest> dsTruyCap = kqBaiTestService.getKQBaiTestByBaiTestId(baiTestId);
            
            // Thêm dữ liệu vào model
            model.addAttribute("baiTest", baiTest);
            model.addAttribute("dsTruyCap", dsTruyCap);
            
            return "nhatuyendung/QLTruyCapTest/index";
        } catch (Exception e) {
            return "redirect:/nhatd/dsbaitest";
        }
    }

    // Thêm phương thức hiển thị form thêm đơn ứng tuyển vào kết quả bài test
    @GetMapping("/nhatd/add-truycap/{baiTestId}")
    public String nhaTDAddTruyCap(@PathVariable Long baiTestId, Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin bài test
            BaiTest baiTest = baiTestService.getBaiTestById(baiTestId);
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() == null || 
                !baiTest.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                return "redirect:/nhatd/dsbaitest";
            }
            
            // Lấy danh sách đơn ứng tuyển đang chờ bài test thuộc tin tuyển dụng của bài test này
            List<DonUngTuyen> dsDonChoTest = donUngTuyenService.getDonUngTuyenByTrangThaiVaTinTD("chotest", baiTest.getTinTuyenDung().getId());
            
            // Lọc ra những đơn chưa có kết quả bài test này
            List<KQBaiTest> dsTruyCap = kqBaiTestService.getKQBaiTestByBaiTestId(baiTestId);
            Set<Long> donDaCoTruyCap = dsTruyCap.stream()
                .map(kq -> kq.getDonUngTuyen().getId())
                .collect(Collectors.toSet());
            
            List<DonUngTuyen> dsDonChuaCoTruyCap = dsDonChoTest.stream()
                .filter(don -> !donDaCoTruyCap.contains(don.getId()))
                .collect(Collectors.toList());
            
            // Thêm dữ liệu vào model
            model.addAttribute("baiTest", baiTest);
            model.addAttribute("dsDonUngTuyen", dsDonChuaCoTruyCap);
            
            return "nhatuyendung/QLTruyCapTest/add";
        } catch (Exception e) {
            return "redirect:/nhatd/dsbaitest";
        }
    }

    // Thêm phương thức xử lý thêm đơn ứng tuyển vào kết quả bài test
    @PostMapping("/nhatd/add-truycap/{baiTestId}")
    public String nhaTDAddTruyCap(@PathVariable Long baiTestId, 
                                @RequestParam("selectedDons") List<Long> donIds,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin bài test
            BaiTest baiTest = baiTestService.getBaiTestById(baiTestId);
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (baiTest.getTinTuyenDung() == null || 
                !baiTest.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền quản lý bài test này!");
                return "redirect:/nhatd/dsbaitest";
            }
            
            // Kiểm tra danh sách đơn ứng tuyển
            if (donIds == null || donIds.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một ứng viên!");
                return "redirect:/nhatd/them-truycap/" + baiTestId;
            }
            
            // Thêm quyền truy cập bài test cho các đơn ứng tuyển được chọn
            int count = kqBaiTestService.themQuyenTruyCapBaiTest(donIds, baiTestId);
            
            redirectAttributes.addFlashAttribute("success", "Đã thêm " + count + " ứng viên vào danh sách truy cập bài test!");
            return "redirect:/nhatd/ds-truycap/" + baiTestId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/them-truycap/" + baiTestId;
        }
    }

    @GetMapping("/nhatd/dskqbaitest/{tinTuyenDungId}")
    public String nhaTDShowDSKetQuaBaiTest(@PathVariable Long tinTuyenDungId, Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin tin tuyển dụng
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(tinTuyenDungId);
            
            // Kiểm tra tin tuyển dụng có thuộc công ty của user không
            if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                return "redirect:/nhatd/dstintd";
            }
            
            List<DonUngTuyen> dsDonCoQuyenTest = donUngTuyenService.getDonUngTuyenByQuyenTestVaTinTD(true, tinTuyenDungId);
            
            // Lấy tất cả bài test thuộc tin tuyển dụng này
            List<BaiTest> dsBaiTest = baiTestService.getBaiTestByTinTuyenDungId(tinTuyenDungId);
            
            // Tạo map lưu kết quả bài test theo loại bài test cho mỗi đơn ứng tuyển
            Map<Long, Map<String, KQBaiTest>> ketQuaByDonIdAndLoai = new HashMap<>();
            
            // Duyệt qua từng đơn ứng tuyển
            for (DonUngTuyen don : dsDonCoQuyenTest) {
                Map<String, KQBaiTest> ketQuaByLoai = new HashMap<>();
                
                // Duyệt qua danh sách kết quả của đơn ứng tuyển
                if (don.getDSKQBaiTest() != null && !don.getDSKQBaiTest().isEmpty()) {
                    for (KQBaiTest kq : don.getDSKQBaiTest()) {
                        // Lưu kết quả bài test theo loại
                        ketQuaByLoai.put(kq.getBaiTest().getLoai(), kq);
                    }
                }
                
                // Lưu kết quả cho đơn ứng tuyển hiện tại
                ketQuaByDonIdAndLoai.put(don.getId(), ketQuaByLoai);
            }
            
            
            // Thêm dữ liệu vào model
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            model.addAttribute("dsDonCoQuyenTest", dsDonCoQuyenTest);
            model.addAttribute("dsBaiTest", dsBaiTest);
            model.addAttribute("ketQuaByDonIdAndLoai", ketQuaByDonIdAndLoai);
            return "nhatuyendung/QLKQBaiTest/index";
        } catch (Exception e) {
            return "redirect:/nhatd/dstintd";
        }
    }
    
    @GetMapping("/nhatd/edit-kqtest/{donId}")
    public String showCapNhatDiemForm(@PathVariable Long donId, Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                model.addAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd";
            }
            
            // Lấy đơn ứng tuyển
            DonUngTuyen don = donUngTuyenService.getDonUngTuyenById(donId);
            
            // Kiểm tra xem đơn ứng tuyển có thuộc tin tuyển dụng của công ty không
            if (don == null || don.getTinTuyenDung().getCongty().getId() != userHienTai.getCongTy().getId()) {
                model.addAttribute("error", "Bạn không có quyền truy cập đơn ứng tuyển này!");
                return "redirect:/nhatd/dstintuyendung";
            }
            
            // Lấy tin tuyển dụng
            TinTuyenDung tinTuyenDung = don.getTinTuyenDung();
            
            // Lấy danh sách kết quả bài test của đơn ứng tuyển này
            Map<Long, KQBaiTest> ketQuaTests = new HashMap<>();
            List<KQBaiTest> dsKQBaiTest = kqBaiTestService.getKQBaiTestsByDonUngTuyenId(donId);
            
            for (KQBaiTest kq : dsKQBaiTest) {
                ketQuaTests.put(kq.getId(), kq);
            }
            
            // Truyền dữ liệu cho view
            model.addAttribute("don", don);
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            model.addAttribute("ketQuaTests", ketQuaTests);
            
            return "nhatuyendung/QLKQBaiTest/CapNhatDiem";
        } catch (Exception e) {
            model.addAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/nhatd/dstintuyendung";
        }
    }

@PostMapping("/nhatd/edit-kqtest/{donId}")
public String saveCapNhatDiem(@PathVariable Long donId, 
                             @RequestParam Map<String, String> allParams,
                             Model model, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        // Lấy đơn ứng tuyển
        DonUngTuyen don = donUngTuyenService.getDonUngTuyenById(donId);
    try {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        // Kiểm tra xem user đã có công ty chưa
        if (userHienTai.getCongTy() == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
            return "redirect:/nhatd";
        }
        
        
        // Kiểm tra xem đơn ứng tuyển có thuộc tin tuyển dụng của công ty không
        if (don == null || don.getTinTuyenDung().getCongty().getId() != userHienTai.getCongTy().getId()) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập đơn ứng tuyển này!");
            return "redirect:/nhatd/dstintuyendung";
        }
        
        // Xử lý cập nhật điểm từ form
        Map<String, String> diemMap = new HashMap<>();
        
        // Tách các tham số thành map điểm
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("diem[") && entry.getKey().endsWith("]")) {
                String kqId = entry.getKey().substring(5, entry.getKey().length() - 1);
                diemMap.put(kqId, entry.getValue());
            }
        }
        
        // Cập nhật từng kết quả bài test
        for (Map.Entry<String, String> entry : diemMap.entrySet()) {
            Long kqId = Long.parseLong(entry.getKey());
            int diem = Integer.parseInt(entry.getValue());
            
            // Kiểm tra điểm hợp lệ
            if (diem < 0 || diem > 100) {
                redirectAttributes.addFlashAttribute("error", "Điểm phải từ 0 đến 100!");
                return "redirect:/nhatd/capnhat-diem/" + donId;
            }
            
            // Lấy và cập nhật kết quả bài test
            KQBaiTest kqBaiTest = kqBaiTestService.getKQBaiTestById(kqId);
            
            // Kiểm tra kết quả bài test có tồn tại không
            if (kqBaiTest == null) {
                continue;
            }
            
            // Kiểm tra kết quả bài test có thuộc đơn ứng tuyển không
            if (!kqBaiTest.getDonUngTuyen().getId().equals(donId)) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền cập nhật kết quả bài test này!");
                return "redirect:/nhatd/kqbaitest/" + don.getTinTuyenDung().getId();
            }
            
            // Cập nhật điểm
            kqBaiTest.setDiem(diem);
            
            // Lưu thay đổi
            kqBaiTestService.updateKQBaiTest(kqBaiTest);
        }
        
        redirectAttributes.addFlashAttribute("success", "Cập nhật điểm thành công!");
        return "redirect:/nhatd/dskqbaitest/" + don.getTinTuyenDung().getId();
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        return "redirect:/nhatd/dskqbaitest/" + don.getTinTuyenDung().getId();
    }
}

    @GetMapping("/nhatd/delete-kqbaitest/{kqId}")
    public String nhaTDDeleteKQBaiTest(@PathVariable Long kqId,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy kết quả bài test
            KQBaiTest kqBaiTest = kqBaiTestService.getKQBaiTestById(kqId);
            
            // Lấy bài test ID để redirect sau khi xóa
            Long tinTuyenDungId = kqBaiTest.getBaiTest().getTinTuyenDung().getId();
            
            // Kiểm tra bài test có thuộc công ty của user không
            if (kqBaiTest.getBaiTest().getTinTuyenDung() == null || 
                !kqBaiTest.getBaiTest().getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền quản lý bài test này!");
                return "redirect:/nhatd/dsbaitest";
            }
            
            // Xử lý xóa quyền truy cập
            String tenUngVien = kqBaiTest.getDonUngTuyen().getUser().getHoTen();
            kqBaiTestService.deleteKQBaiTest(kqId);
            
            redirectAttributes.addFlashAttribute("success", "Đã xóa quyền truy cập bài test cho ứng viên " + tenUngVien);
            return "redirect:/nhatd/dskqbaitest/" + tinTuyenDungId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/dsbaitest";
        }
    }

    @PostMapping("/api/receive-score")
    public ResponseEntity<String> receiveScore(@RequestBody ScoreDTO score) {
        log.info("Nhận kết quả bài test: {}", score);
        
        try {
            boolean success = diemService.save(score);
            if (success) {
                return ResponseEntity.ok("Đã cập nhật điểm thành công");
            } else {
                return ResponseEntity.badRequest().body("Không thể cập nhật điểm");
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý kết quả bài test", e);
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/nvhs/dskqbaitest/{tinTuyenDungId}")
    public String nVHSShowDSKetQuaBaiTest(@PathVariable Long tinTuyenDungId, Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nvhs";
            }
            
            // Lấy thông tin tin tuyển dụng
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(tinTuyenDungId);
            
            // Kiểm tra tin tuyển dụng có thuộc công ty của user không
            if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                return "redirect:/nvhs/dstintd";
            }
            
            List<DonUngTuyen> dsDonCoQuyenTest = donUngTuyenService.getDonUngTuyenByQuyenTestVaTinTD(true, tinTuyenDungId);
            
            // Lấy tất cả bài test thuộc tin tuyển dụng này
            List<BaiTest> dsBaiTest = baiTestService.getBaiTestByTinTuyenDungId(tinTuyenDungId);
            
            // Tạo map lưu kết quả bài test theo loại bài test cho mỗi đơn ứng tuyển
            Map<Long, Map<String, KQBaiTest>> ketQuaByDonIdAndLoai = new HashMap<>();
            
            // Duyệt qua từng đơn ứng tuyển
            for (DonUngTuyen don : dsDonCoQuyenTest) {
                Map<String, KQBaiTest> ketQuaByLoai = new HashMap<>();
                
                // Duyệt qua danh sách kết quả của đơn ứng tuyển
                if (don.getDSKQBaiTest() != null && !don.getDSKQBaiTest().isEmpty()) {
                    for (KQBaiTest kq : don.getDSKQBaiTest()) {
                        // Lưu kết quả bài test theo loại
                        ketQuaByLoai.put(kq.getBaiTest().getLoai(), kq);
                    }
                }
                
                // Lưu kết quả cho đơn ứng tuyển hiện tại
                ketQuaByDonIdAndLoai.put(don.getId(), ketQuaByLoai);
            }
            
            
            // Thêm dữ liệu vào model
            model.addAttribute("tinTuyenDung", tinTuyenDung);
            model.addAttribute("dsDonCoQuyenTest", dsDonCoQuyenTest);
            model.addAttribute("dsBaiTest", dsBaiTest);
            model.addAttribute("ketQuaByDonIdAndLoai", ketQuaByDonIdAndLoai);
            return "nvhs/QLKQBaiTest/index";
        } catch (Exception e) {
            return "redirect:/nvhs/dstintd";
        }
    }
}
