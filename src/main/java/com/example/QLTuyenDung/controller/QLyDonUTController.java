package com.example.QLTuyenDung.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.example.QLTuyenDung.service.FileStorageService;
import com.example.QLTuyenDung.service.HocVanService;
import com.example.QLTuyenDung.service.KinhNghiemLamViecService;
import com.example.QLTuyenDung.service.PhongVanService;
import com.example.QLTuyenDung.service.ThanhTuuService;
import com.example.QLTuyenDung.service.TinTuyenDungService;
import com.example.QLTuyenDung.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class QLyDonUTController {
    private final DonUngTuyenService donUngTuyenService;
    private final UserService userService;
    private final TinTuyenDungService tinTuyenDungService;
    private final PhongVanService phongVanService;
    private final FileStorageService fileStorageService;
    private final HocVanService hocVanService;
    private final KinhNghiemLamViecService kinhNghiemLamViecService;
    private final ThanhTuuService thanhTuuService;

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
    public String nhaTDUpdateTrangThaiDonUT(@PathVariable Long id, 
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

    @GetMapping("/ungvien/dsdonut")
    public String ungVienShowDSDonUT(Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Lấy danh sách đơn ứng tuyển của user hiện tại
            List<DonUngTuyen> danhSachDonUT = donUngTuyenService.getDonUngTuyenByUserId(userHienTai.getId());
            
            model.addAttribute("dsDonUT", danhSachDonUT);
            model.addAttribute("statusConverter", donUngTuyenService);
            
            return "ungvien/QLDonUngTuyen/index";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @GetMapping("/ungvien/add-donut/{donId}")
    public String ungVienAddDonUT(@PathVariable Long donId, 
                            Authentication authentication,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {
        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication == null) {
            // Lưu URL hiện tại để sau khi đăng nhập chuyển về
            String redirectUrl = "/ungvien/add-donut/" + donId;
            return "redirect:/login?redirect=" + redirectUrl;
        }
        
        try {
            // Lấy thông tin người dùng
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra vai trò Candidate
            boolean isCandidate = userHienTai.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("CANDIDATE"));
                    
            if (!isCandidate) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần có tài khoản ứng viên để ứng tuyển!");
                return "redirect:/chitiet-tintd/" + donId;
            }
            
            // Kiểm tra CV
            if (userHienTai.getCvFile() == null || userHienTai.getCvFile().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần tải lên CV trước khi ứng tuyển!");
                return "redirect:/ungvien/edit-user";
            }
            
            // Lấy thông tin tin tuyển dụng
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(donId);
            
            // Tạo đơn ứng tuyển
            DonUngTuyen donUngTuyen = new DonUngTuyen();
            donUngTuyen.setUser(userHienTai);
            donUngTuyen.setTinTuyenDung(tinTuyenDung);

            // Sao chép file CV từ user
            String originalCV = userHienTai.getCvFile();
            if (originalCV != null && !originalCV.isEmpty()) {
                try {
                    // Tạo bản sao của CV
                    String copiedCV = fileStorageService.copyCV(originalCV);
                    donUngTuyen.setCvFile(copiedCV);
                } catch (Exception e) {
                    // Nếu có lỗi khi sao chép, sử dụng CV gốc
                    donUngTuyen.setCvFile(originalCV);
                    System.err.println("Lỗi khi sao chép CV: " + e.getMessage());
                }
            }
            
            // Sử dụng service để lưu (đã có kiểm tra trùng lặp trong service)
            try {
                donUngTuyenService.addDonUngTuyen(donUngTuyen);
                redirectAttributes.addFlashAttribute("success", "Ứng tuyển thành công! Tin của bạn đã được gửi đến nhà tuyển dụng.");
                return "redirect:/ungvien/dsdonut";
            } catch (RuntimeException e) {
                if (e.getMessage().contains("đã ứng tuyển")) {
                    redirectAttributes.addFlashAttribute("error", e.getMessage());
                    return "redirect:/ungvien/dsdonut";
                } else {
                    throw e;
                }
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/chitiet-tintd/" + donId;
        }
    }

    @GetMapping("/ungvien/delete-donut/{id}")
    public String ungVienDeleteDonUT(@PathVariable Long id, 
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Lấy thông tin đơn ứng tuyển
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(id);
            
            // Kiểm tra đơn có thuộc về user hiện tại không
            if (!donUngTuyen.getUser().getId().equals(userHienTai.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa đơn ứng tuyển này!");
                return "redirect:/ungvien/dsdonut";
            }
            
            // Kiểm tra trạng thái đơn ứng tuyển
            if (donUngTuyen.getTrangThai().equals("datuyen")) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa đơn ứng tuyển đã được tuyển!");
                return "redirect:/ungvien/dsdonut";
            }
            
            // Xóa đơn ứng tuyển
            donUngTuyenService.deleteDonUngTuyen(id);
            
            redirectAttributes.addFlashAttribute("success", "Đã xóa đơn ứng tuyển thành công!");
            return "redirect:/ungvien/dsdonut";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/ungvien/dsdonut";
        }
    }
    @GetMapping("/nvhs/dsdonut/{tinTdId}")
    public String nVHSShowDSDonUTTheoTinTD(@PathVariable Long tinTdId, Model model, Authentication authentication) {
        try {
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                return "redirect:/nvhs";
            }
            
            // Lấy thông tin tin tuyển dụng
            TinTuyenDung tinTuyenDung = tinTuyenDungService.getTinTuyenDungById(tinTdId);
            
            // Kiểm tra tin tuyển dụng có thuộc công ty của user không
            if (!tinTuyenDung.getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                return "redirect:/nvhs/dstintd";
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
            
            return "nvhs/QLDonUngTuyen/index";
        } catch (Exception e) {
            return "redirect:/nvhs/dstintd";
        }
    }

    @GetMapping("/nvhs/update-trangthai/{id}/{trangThai}")
    public String nVHSUpdateTrangThaiDonUT(@PathVariable Long id, 
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
                return "redirect:/nvhs";
            }
            
            // Lấy thông tin đơn ứng tuyển
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(id);
            
            // Kiểm tra đơn có thuộc về tin tuyển dụng của công ty user không
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền cập nhật đơn ứng tuyển này!");
                return "redirect:/nvhs/dstintd";
            }
            
            // Kiểm tra trạng thái hợp lệ
            if (!trangThai.equals("dangduyet") && !trangThai.equals("chotest") && !trangThai.equals("phongvan") 
                && !trangThai.equals("datuyen") && !trangThai.equals("tuchoi")) {
                redirectAttributes.addFlashAttribute("error", "Trạng thái không hợp lệ!");
                return "redirect:/nvhs/dsdonut/" + donUngTuyen.getTinTuyenDung().getId();
            }
            
            // Lưu trạng thái cũ để thông báo
            String statusMessage = donUngTuyenService.getStatusMessage(trangThai);
            
            // Cập nhật trạng thái
            donUngTuyen.setTrangThai(trangThai);
            if(donUngTuyenService.kiemTraTrangThaiPV(donUngTuyen)){
                redirectAttributes.addFlashAttribute("info", 
                        "Đã tự động tạo phỏng vấn mới cho ứng viên này.");
            }
            donUngTuyenService.updateDonUngTuyen(donUngTuyen);
            
            redirectAttributes.addFlashAttribute("success", "Đã chuyển trạng thái đơn ứng tuyển sang " + statusMessage);
            return "redirect:/nvhs/dsdonut/" + donUngTuyen.getTinTuyenDung().getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return "redirect:/nvhs/dstintd";
        }
    }

    // Cập nhật phương thức showHoSoUngTuyen
    @GetMapping("/hosoungtuyen/{id}")
    public String showHoSoUngTuyen(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            // Nếu không đăng nhập, từ chối truy cập
            if (authentication == null) {
                return "redirect:/login";
            }
            
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User currentUser = userDetails.getUser();
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(id);
            
            // Kiểm tra quyền truy cập
            boolean hasAccess = false;
            
            // Trường hợp 1: User đang xem là chủ sở hữu đơn
            if (donUngTuyen.getUser().getId().equals(currentUser.getId())) {
                hasAccess = true;
            } 
            // Trường hợp 2: User là nhà tuyển dụng, nhân viên tuyển dụng hoặc nhân viên hồ sơ của công ty
            else if (currentUser.getCongTy() != null && 
                    donUngTuyen.getTinTuyenDung().getCongty().getId().equals(currentUser.getCongTy().getId())) {
                
                // Kiểm tra vai trò của user
                boolean isCvStaff = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("CV_STAFF"));
                boolean isRecruiter = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("RECRUITER"));
                boolean isHrStaff = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("HR_STAFF"));
                    
                hasAccess = isCvStaff || isRecruiter || isHrStaff;
            }
            
            if (!hasAccess) {
                return "redirect:/error/403";
            }
            
            // Nếu có quyền truy cập, hiển thị thông tin
            model.addAttribute("don", donUngTuyen);
            model.addAttribute("dSHocVan", hocVanService.getHocVanByUserId(donUngTuyen.getUser().getId()));
            model.addAttribute("dSKinhNghiem", kinhNghiemLamViecService.getKinhNghiemLamViecByUserId(donUngTuyen.getUser().getId()));
            model.addAttribute("dSThanhTuu", thanhTuuService.getThanhTuuByUserId(donUngTuyen.getUser().getId()));
            return "user/HoSoUngTuyen";
        } catch (RuntimeException e) {
            return "redirect:/";
        }
    }

    // Cập nhật phương thức downloadCVCuaDonUT
    @GetMapping("/download-cvdon/{id}")
    public ResponseEntity<?> downloadCVCuaDonUT(@PathVariable Long id, Authentication authentication) {
        try {
            // Nếu không đăng nhập, từ chối truy cập
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
            }
            
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User currentUser = userDetails.getUser();
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(id);
            
            // Kiểm tra quyền truy cập
            boolean hasAccess = false;
            
            // Trường hợp 1: User đang xem là chủ sở hữu đơn
            if (donUngTuyen.getUser().getId().equals(currentUser.getId())) {
                hasAccess = true;
            } 
            // Trường hợp 2: User là nhà tuyển dụng, nhân viên tuyển dụng hoặc nhân viên hồ sơ của công ty
            else if (currentUser.getCongTy() != null && 
                    donUngTuyen.getTinTuyenDung().getCongty().getId().equals(currentUser.getCongTy().getId())) {
                
                // Kiểm tra vai trò của user
                boolean isCvStaff = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("CV_STAFF"));
                boolean isRecruiter = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("RECRUITER"));
                boolean isHrStaff = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getName().equalsIgnoreCase("HR_STAFF"));
                    
                hasAccess = isCvStaff || isRecruiter || isHrStaff;
            }
            
            if (!hasAccess) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            
            // Nếu có quyền truy cập, tiến hành tải xuống file CV
            if (donUngTuyen != null && donUngTuyen.getCvFile() != null) {
                Path cvPath = Paths.get(fileStorageService.getUploadDir() + File.separator + donUngTuyen.getCvFile());
                if (Files.exists(cvPath)) {
                    byte[] data = Files.readAllBytes(cvPath);
                    return ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=\"" + donUngTuyen.getCvFile() + "\"")
                        .body(data);
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error downloading file");
        }
    }
}
