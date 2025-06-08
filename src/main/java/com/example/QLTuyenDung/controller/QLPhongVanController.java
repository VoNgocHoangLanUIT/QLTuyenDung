package com.example.QLTuyenDung.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
public class QLPhongVanController {
    private final PhongVanService phongVanService;
    private final UserService userService;
    private final DonUngTuyenService donUngTuyenService;
    private final TinTuyenDungService tinTuyenDungService;

    @GetMapping("/admin/dsphongvan")
    public String adminShowDSPhongVan(Model model) {
        List<PhongVan> dsPhongVan = phongVanService.getAllPhongVan();
        
        // Group by donUngTuyen.id
        Map<Long, List<PhongVan>> groupedByDon = dsPhongVan.stream()
        .collect(Collectors.groupingBy(pv -> pv.getDonUngTuyen().getId()));
    
        model.addAttribute("groupedByDon", groupedByDon);
        return "admin/QLQuyTrinhTuyenDung/QLPhongVan/index";
    }

    @GetMapping("/admin/get-nhanvien-by-congty")
    @ResponseBody
    public List<User> adminGetNhanVienByCongTy(@RequestParam Long tinTuyenDungId, @RequestParam Long donUngTuyenId) {
        try {
            List<User> allStaff = userService.getNhanVienTuyenDungByCongTyId(tinTuyenDungId);

            List<User> assignedStaff = phongVanService.getNhanVienByDonUngTuyenId(donUngTuyenId);
            // Filter and return only necessary data
            List<User> availableStaff = allStaff.stream()
            .filter(staff -> assignedStaff.stream()
                .noneMatch(assigned -> assigned.getId().equals(staff.getId())))
            .map(user -> {
                User simpleUser = new User();
                simpleUser.setId(user.getId());
                simpleUser.setHoTen(user.getHoTen());
                return simpleUser;
            })
            .collect(Collectors.toList());
            return availableStaff;
        } catch (Exception e) {
            System.err.println("Error in getNhanVienByCongTy: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @PostMapping("/admin/phancongpv")
    public String adminPhanCongPhongVan(@RequestParam Long phongVanId,
                            @RequestParam Long tinTuyenDungId,
                            @RequestParam List<Long> nhanVienIds,
                            RedirectAttributes redirectAttributes) {
        try {
            // Validate that selected staff belong to the correct company
            List<User> dSNVTDCuaCongTy = userService.getNhanVienTuyenDungByCongTyId(tinTuyenDungId);
            boolean kTraNVTDDaChon = nhanVienIds.stream()
                .allMatch(nvId -> dSNVTDCuaCongTy.stream()
                    .anyMatch(v -> v.getId().equals(nvId)));
            
            if (!kTraNVTDDaChon) {
                redirectAttributes.addFlashAttribute("error", 
                    "Một số nhân viên không thuộc công ty này");
                return "redirect:/admin/dsphongvan";
            }

            phongVanService.phanCongPhongVan(phongVanId, nhanVienIds);
            redirectAttributes.addFlashAttribute("success", 
                "Thêm nhân viên phỏng vấn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/dsphongvan";
    }
    @GetMapping("/admin/delete-nvtd/{pvId}/{nvId}")
    public String adminDeleteNVTDCuaPhongVan(@PathVariable Long pvId, 
                                        @PathVariable Long nvId,
                                        RedirectAttributes redirectAttributes) {
        try {
            PhongVan phongVan = phongVanService.getPhongVanById(pvId);
            
            // Check if this staff is assigned to this interview
            if (phongVan.getNhanVienTD() == null || !phongVan.getNhanVienTD().getId().equals(nvId)) {
                redirectAttributes.addFlashAttribute("error", 
                    "Nhân viên không được phân công cho cuộc phỏng vấn này");
                return "redirect:/admin/dsphongvan";
            }
            
            // Remove the staff from interview
            phongVanService.deleteNVTDCuaPhongVan(pvId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đã xóa nhân viên khỏi cuộc phỏng vấn");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/dsphongvan";
    }

    @GetMapping("/admin/chitiet-phongvan/{id}")
    public String adminChiTietPhongVan(@PathVariable Long id, Model model) {
        try {
            // Lấy thông tin phỏng vấn gốc
            PhongVan phongVan = phongVanService.getPhongVanById(id);
            if (phongVan == null) {
                throw new RuntimeException("Không tìm thấy phỏng vấn");
            }
            
            // Lấy thông tin đơn ứng tuyển
            DonUngTuyen donUngTuyen = phongVan.getDonUngTuyen();
            
            // Lấy tất cả các phỏng vấn của đơn ứng tuyển này, sắp xếp theo thời gian
            List<PhongVan> dSPhongVan = phongVanService.getPhongVanByDonUngTuyen(donUngTuyen);
            
            dSPhongVan.sort(
                Comparator
                    // Sắp xếp theo có nhân viên hay không (không có nhân viên xuống cuối)
                    .comparing((PhongVan pv) -> pv.getNhanVienTD() == null)
                    // Sau đó sắp xếp theo thời gian (null xuống cuối)
                    .thenComparing(PhongVan::getNgayPV, Comparator.nullsLast(Comparator.naturalOrder()))
            );
            // Truyền dữ liệu vào view
            model.addAttribute("donUngTuyen", donUngTuyen);
            model.addAttribute("listPhongVan", dSPhongVan);
            
            return "admin/QLQuyTrinhTuyenDung/QLPhongVan/index2";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/dsphongvan";
        }
    }

    
    @PostMapping("/admin/capnhat-lich-phongvan")
    public String adminCapNhatLichPhongVan(
            @RequestParam Long phongVanId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date ngayPV,
            @RequestParam(required = false) String diaDiem,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Lấy thông tin phỏng vấn
            PhongVan phongVan = phongVanService.getPhongVanById(phongVanId);
            if (phongVan == null) {
                throw new RuntimeException("Không tìm thấy phỏng vấn");
            }
            
            // Cập nhật thông tin lịch
            phongVan.setNgayPV(ngayPV);
            phongVan.setDiaDiem(diaDiem);
            
            // Lưu thay đổi
            phongVanService.save(phongVan);
            
            // Thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật lịch phỏng vấn thành công");
            
            // Redirect về trang chi tiết
            return "redirect:/admin/chitiet-phongvan/" + phongVanId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/dsphongvan";
        }
    }

    @PostMapping("/admin/danh-gia-pv")
    public String adminDanhGiaPhongVan(
            @RequestParam Long phongVanId,
            @RequestParam int diemDanhGia,
            @RequestParam(required = false) String nhanXet,
            @RequestParam String trangThai,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Lấy thông tin phỏng vấn
            PhongVan phongVan = phongVanService.getPhongVanById(phongVanId);
            if (phongVan == null) {
                throw new RuntimeException("Không tìm thấy phỏng vấn");
            }
            
            // Kiểm tra điểm đánh giá hợp lệ
            if (diemDanhGia < 0 || diemDanhGia > 10) {
                throw new RuntimeException("Điểm đánh giá phải nằm trong khoảng 0-10");
            }
            
            // Cập nhật thông tin đánh giá
            phongVan.setDiemDanhGia(diemDanhGia);
            phongVan.setNhanXet(nhanXet);
            phongVan.setTrangThai(trangThai);
            
            // Lưu thay đổi
            phongVanService.save(phongVan);
            
            // Thông báo thành công
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật đánh giá phỏng vấn thành công");
            
            // Redirect về trang chi tiết
            return "redirect:/admin/chitiet-phongvan/" + phongVanId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/chitiet-phongvan/" + phongVanId;
        }
    }

    @GetMapping("/nhatd/dsphongvan")
    public String nhaTDShowDSPhongVan(Model model, Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra user có công ty không
            if (userHienTai.getCongTy() == null) {
                model.addAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy tin tuyển dụng của công ty
            List<TinTuyenDung> dSTinTD = tinTuyenDungService.getTinTuyenDungByCongTy(userHienTai.getCongTy());
            
            // Lấy tất cả đơn ứng tuyển có trạng thái phỏng vấn ("phongvan")
            List<DonUngTuyen> dSDonUTPhongVan = new ArrayList<>();
            for (TinTuyenDung tin : dSTinTD) {
                List<DonUngTuyen> donUT = donUngTuyenService.getDonUngTuyenByTrangThaiVaTinTD("phongvan", tin.getId());
                dSDonUTPhongVan.addAll(donUT);
            }
            
            // Tạo Map để nhóm các cuộc phỏng vấn theo đơn ứng tuyển
            Map<DonUngTuyen, List<PhongVan>> phongVanByDonUT = new HashMap<>();
            for (DonUngTuyen don : dSDonUTPhongVan) {
                List<PhongVan> dsPhongVan = phongVanService.getPhongVanByDonUngTuyen(don);
                if (!dsPhongVan.isEmpty()) {
                    phongVanByDonUT.put(don, dsPhongVan);
                }
            }
            
            model.addAttribute("phongVanByDonUT", phongVanByDonUT);
            model.addAttribute("dSTinTD", dSTinTD);
            return "nhatuyendung/QLPhongVan/index";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "nhatuyendung/QLPhongVan/index";
        }
    }

    @GetMapping("/nhatd/add-phongvan/{donUngTuyenId}")
    public String nhaTDAddPhongVan(@PathVariable Long donUngTuyenId, 
                                        Model model, Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                model.addAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy đơn ứng tuyển
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(donUngTuyenId);
            
            // Kiểm tra quyền
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                model.addAttribute("error", "Bạn không có quyền quản lý đơn ứng tuyển này!");
                return "redirect:/nhatd/dsphongvan";
            }
            
            // Lấy danh sách nhân viên tuyển dụng
            List<User> danhSachNhanVien = userService.getNhanVienChuaPhanCongPhongVan(donUngTuyenId, userHienTai.getCongTy().getId());
            System.out.println("Danh sách nhân viên chưa phân công: ");
            model.addAttribute("donUngTuyen", donUngTuyen);
            model.addAttribute("danhSachNhanVien", danhSachNhanVien);
            
            return "nhatuyendung/QLPhongVan/add";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/dsphongvan";
        }
    }

    @PostMapping("/nhatd/add-phongvan")
    public String nhaTDAddPhongVan(@RequestParam Long donUngTuyenId,
                                        @RequestParam List<Long> selectedStaff,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date ngayPV,
                                        @RequestParam String diaDiem,
                                        RedirectAttributes redirectAttributes,
                                        Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Kiểm tra danh sách nhân viên có trống không
            if (selectedStaff == null || selectedStaff.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần chọn ít nhất một nhân viên tuyển dụng!");
                return "redirect:/nhatd/phancong-phongvan/" + donUngTuyenId;
            }
            
            // Lấy đơn ứng tuyển
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(donUngTuyenId);
            
            // Kiểm tra quyền
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền quản lý đơn ứng tuyển này!");
                return "redirect:/nhatd/dsphongvan";
            }
            
            // Thực hiện phân công
            List<PhongVan> phongVan = phongVanService.nhaTDPhanCongPhongVan(
                donUngTuyenId, selectedStaff, ngayPV, diaDiem);
            
            redirectAttributes.addFlashAttribute("success", 
                "Đã phân công " + phongVan.size() + " nhân viên tham gia phỏng vấn thành công!");
            
            return "redirect:/nhatd/dsphongvan";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/phancong-phongvan/" + donUngTuyenId;
        }
    }

    @GetMapping("/nhatd/edit-phongvan/{id}")
    public String nhaTDEditPhongVan(@PathVariable Long id, 
                                    Model model, Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                model.addAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin phỏng vấn
            PhongVan phongVan = phongVanService.getPhongVanById(id);
            DonUngTuyen donUngTuyen = phongVan.getDonUngTuyen();
            
            // Kiểm tra quyền (phỏng vấn phải thuộc tin tuyển dụng của công ty người dùng)
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                model.addAttribute("error", "Bạn không có quyền quản lý cuộc phỏng vấn này!");
                return "redirect:/nhatd/dsphongvan";
            }
            
            // Lấy danh sách nhân viên có thể được phân công
            // Bao gồm nhân viên hiện tại và các nhân viên chưa được phân công
            List<User> danhSachNhanVien = userService.getNhanVienChuaPhanCongPhongVan(
                donUngTuyen.getId(), userHienTai.getCongTy().getId());
            
            // Thêm nhân viên hiện tại đang phỏng vấn nếu không nằm trong danh sách
            if (phongVan.getNhanVienTD() != null) {
                boolean isNhanVienTDInList = danhSachNhanVien.stream()
                    .anyMatch(nv -> nv.getId().equals(phongVan.getNhanVienTD().getId()));
                    
                if (!isNhanVienTDInList) {
                    danhSachNhanVien.add(phongVan.getNhanVienTD());
                }
            }
            
            model.addAttribute("phongVan", phongVan);
            model.addAttribute("donUngTuyen", donUngTuyen);
            model.addAttribute("danhSachNhanVien", danhSachNhanVien);
            model.addAttribute("trangThaiOptions", Arrays.asList("chopv", "dapv"));
            
            return "nhatuyendung/QLPhongVan/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/dsphongvan";
        }
    }

    @PostMapping("/nhatd/edit-phongvan")
    public String nhaTDEditPhongVan(@RequestParam Long phongVanId,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") Date ngayPV,
                                @RequestParam(required = false) String diaDiem,
                                @RequestParam(required = false) String trangThai,
                                @RequestParam(required = false) Long nhanVienId,
                                @RequestParam(required = false, defaultValue = "0") Integer diemDanhGia,
                                @RequestParam(required = false) String nhanXet,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin phỏng vấn
            PhongVan phongVan = phongVanService.getPhongVanById(phongVanId);
            DonUngTuyen donUngTuyen = phongVan.getDonUngTuyen();
            
            // Kiểm tra quyền
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền quản lý cuộc phỏng vấn này!");
                return "redirect:/nhatd/dsphongvan";
            }
            
            // Nếu người dùng chọn nhân viên mới, kiểm tra nhân viên đã được phân công chưa
            if (nhanVienId != null && 
                (phongVan.getNhanVienTD() == null || !nhanVienId.equals(phongVan.getNhanVienTD().getId()))) {
                // Kiểm tra xem nhân viên đã được phân công cho đơn ứng tuyển này chưa
                if (phongVanService.isNhanVienDaPhanCong(nhanVienId, donUngTuyen.getId())) {
                    redirectAttributes.addFlashAttribute("error", 
                        "Nhân viên này đã được phân công cho đơn ứng tuyển hiện tại!");
                    return "redirect:/nhatd/edit-phongvan/" + phongVanId;
                }
            }
            
            // Cập nhật thông tin phỏng vấn
            phongVanService.capNhatPhongVan(phongVanId, ngayPV, diaDiem, trangThai, nhanVienId, diemDanhGia, nhanXet);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin phỏng vấn thành công!");
            return "redirect:/nhatd/dsphongvan";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nhatd/edit-phongvan/" + phongVanId;
        }
    }

    // Thêm endpoint xóa phỏng vấn
    @GetMapping("/nhatd/delete-phongvan/{id}")
    public String nhaTDDeletePhongVan(@PathVariable Long id, 
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                redirectAttributes.addFlashAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin phỏng vấn
            PhongVan phongVan = phongVanService.getPhongVanById(id);
            DonUngTuyen donUngTuyen = phongVan.getDonUngTuyen();
            
            // Kiểm tra quyền (phỏng vấn phải thuộc tin tuyển dụng của công ty người dùng)
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xóa cuộc phỏng vấn này!");
                return "redirect:/nhatd/dsphongvan";
            }
            
            // Xóa phỏng vấn
            phongVanService.deletePhongVan(id);
            
            redirectAttributes.addFlashAttribute("success", "Xóa phỏng vấn thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/nhatd/dsphongvan";
    }

    @GetMapping("/nhatd/chitiet-phongvan/{donUngTuyenId}")
    public String nhaTDChiTietPhongVan(@PathVariable Long donUngTuyenId, 
                                    Model model, 
                                    Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                model.addAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nhatd/edit-congty";
            }
            
            // Lấy thông tin đơn ứng tuyển
            DonUngTuyen donUngTuyen = donUngTuyenService.getDonUngTuyenById(donUngTuyenId);
            if (donUngTuyen == null) {
                throw new RuntimeException("Không tìm thấy đơn ứng tuyển");
            }
            
            // Kiểm tra quyền (đơn ứng tuyển phải thuộc tin tuyển dụng của công ty người dùng)
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                model.addAttribute("error", "Bạn không có quyền xem đơn ứng tuyển này!");
                return "redirect:/nhatd/dsphongvan";
            }
            
            // Lấy tất cả các phỏng vấn của đơn ứng tuyển này
            List<PhongVan> dSPhongVan = phongVanService.getPhongVanByDonUngTuyen(donUngTuyen);
            
            // Sắp xếp theo thứ tự:
            // 1. Các phỏng vấn đã có nhân viên phân công lên trước
            // 2. Sau đó sắp xếp theo thời gian (mới nhất lên đầu)
            dSPhongVan.sort(
                Comparator
                    // Sắp xếp theo có nhân viên hay không (có nhân viên lên đầu)
                    .comparing((PhongVan pv) -> pv.getNhanVienTD() == null)
                    // Sau đó sắp xếp theo thời gian (mới nhất lên đầu, null xuống cuối)
                    .thenComparing(PhongVan::getNgayPV, Comparator.nullsLast(Comparator.reverseOrder()))
            );
            
            // Lấy thông tin ứng viên
            User ungVien = donUngTuyen.getUser();
            
            // Truyền dữ liệu vào view
            model.addAttribute("donUngTuyen", donUngTuyen);
            model.addAttribute("ungVien", ungVien);
            model.addAttribute("listPhongVan", dSPhongVan);
            model.addAttribute("tinTuyenDung", donUngTuyen.getTinTuyenDung());
            
            return "nhatuyendung/QLPhongVan/ChiTiet";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/nhatd/dsphongvan";
        }
    }

    @GetMapping("/ungvien/phong-van")
    public String ungVienPhongVan(Model model, Authentication authentication) {
        try {
            // Lấy thông tin người dùng hiện tại
            CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
            User userHienTai = userDetails.getUser();
            
            // Lấy tất cả đơn ứng tuyển của ứng viên
            List<DonUngTuyen> donUngTuyens = donUngTuyenService.getDonUngTuyenByUserId(userHienTai.getId());
            
            // Lọc ra các đơn có trạng thái phỏng vấn
            List<DonUngTuyen> donUngTuyenPhongVan = donUngTuyens.stream()
                    .filter(don -> "phongvan".equals(don.getTrangThai()))
                    .collect(Collectors.toList());
            // Nhóm phỏng vấn theo tin tuyển dụng
            Map<TinTuyenDung, List<PhongVan>> phongVanByTinTD = new HashMap<>();
            
            for (DonUngTuyen don : donUngTuyenPhongVan) {
                List<PhongVan> dsPhongVan = phongVanService.getPhongVanByDonUngTuyen(don);
                
                if (!dsPhongVan.isEmpty()) {
                    TinTuyenDung tinTD = don.getTinTuyenDung();
                    
                    if (!phongVanByTinTD.containsKey(tinTD)) {
                        phongVanByTinTD.put(tinTD, new ArrayList<>());
                    }
                    
                    phongVanByTinTD.get(tinTD).addAll(dsPhongVan);
                }
            }
            
            model.addAttribute("phongVanByTinTD", phongVanByTinTD);
            return "ungvien/PhongVan/index";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "ungvien/PhongVan/index";
        }
    }

    @GetMapping("/nvtd/dsphongvan")
    public String nVTDShowDSPhongVan(Model model, Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra user có công ty không
            if (userHienTai.getCongTy() == null) {
                model.addAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nvtd";
            }
            
            // Tạo Map để nhóm các cuộc phỏng vấn theo đơn ứng tuyển
            Map<DonUngTuyen, List<PhongVan>> phongVanByDonUT = new HashMap<>();
            
            // Lấy tất cả phỏng vấn được phân công cho nhân viên hiện tại
            List<PhongVan> phongVanCuaNV = phongVanService.getPhongVanByNhanVien(userHienTai);
            
            // Nhóm phỏng vấn theo đơn ứng tuyển
            for (PhongVan pv : phongVanCuaNV) {
                DonUngTuyen donUngTuyen = pv.getDonUngTuyen();
                
                // Đảm bảo chỉ lấy các đơn có trạng thái phỏng vấn
                if ("phongvan".equals(donUngTuyen.getTrangThai())) {
                    if (!phongVanByDonUT.containsKey(donUngTuyen)) {
                        phongVanByDonUT.put(donUngTuyen, new ArrayList<>());
                    }
                    phongVanByDonUT.get(donUngTuyen).add(pv);
                }
            }
            
            model.addAttribute("phongVanByDonUT", phongVanByDonUT);
            return "nvtd/QLPhongVan/index";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "nvtd/QLPhongVan/index";
        }
    }

    @GetMapping("/nvtd/edit-phongvan/{id}")
    public String nVTDUpdatePhongVan(@PathVariable Long id, 
                                    Model model, Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Kiểm tra xem user đã có công ty chưa
            if (userHienTai.getCongTy() == null) {
                model.addAttribute("error", "Bạn cần cập nhật thông tin công ty trước!");
                return "redirect:/nvtd";
            }
            
            // Lấy thông tin phỏng vấn
            PhongVan phongVan = phongVanService.getPhongVanById(id);
            DonUngTuyen donUngTuyen = phongVan.getDonUngTuyen();
            
            // Kiểm tra quyền (phỏng vấn phải thuộc tin tuyển dụng của công ty người dùng)
            if (!donUngTuyen.getTinTuyenDung().getCongty().getId().equals(userHienTai.getCongTy().getId())) {
                model.addAttribute("error", "Bạn không có quyền quản lý cuộc phỏng vấn này!");
                return "redirect:/nvtd/dsphongvan";
            }

            // Kiểm tra quyền (phỏng vấn phải được phân công cho nhân viên hiện tại)
            if (phongVan.getNhanVienTD() == null || !phongVan.getNhanVienTD().getId().equals(userHienTai.getId())) {
                model.addAttribute("error", "Bạn không có quyền cập nhật cuộc phỏng vấn này!");
                return "redirect:/nvtd/dsphongvan";
            }
            
            // Lấy danh sách nhân viên có thể được phân công
            // Bao gồm nhân viên hiện tại và các nhân viên chưa được phân công
            List<User> danhSachNhanVien = userService.getNhanVienChuaPhanCongPhongVan(
                donUngTuyen.getId(), userHienTai.getCongTy().getId());
            
            // Thêm nhân viên hiện tại đang phỏng vấn nếu không nằm trong danh sách
            
            model.addAttribute("phongVan", phongVan);
            model.addAttribute("donUngTuyen", donUngTuyen);
            model.addAttribute("trangThaiOptions", Arrays.asList("chopv", "dapv"));
            
            return "nvtd/QLPhongVan/edit";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nvtd/dsphongvan";
        }
    }

    @PostMapping("/nvtd/update-phongvan")
    public String nVTDUpdatePhongVan(@RequestParam Long phongVanId,
                            @RequestParam(required = false) String trangThai,
                            @RequestParam(required = false, defaultValue = "0") Integer diemDanhGia,
                            @RequestParam(required = false) String nhanXet,
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {
        // Lấy thông tin người dùng hiện tại
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        User userHienTai = userDetails.getUser();
        
        try {
            // Lấy thông tin phỏng vấn
            PhongVan phongVan = phongVanService.getPhongVanById(phongVanId);
            
            // Kiểm tra quyền (phỏng vấn phải được phân công cho nhân viên hiện tại)
            if (phongVan.getNhanVienTD() == null || !phongVan.getNhanVienTD().getId().equals(userHienTai.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền cập nhật cuộc phỏng vấn này!");
                return "redirect:/nvtd/dsphongvan";
            }
            
            // Cập nhật thông tin phỏng vấn (không thay đổi ngày và địa điểm, chỉ cập nhật đánh giá)
            phongVanService.capNhatPhongVan(phongVanId, null, null, trangThai, null, diemDanhGia, nhanXet);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin phỏng vấn thành công!");
            return "redirect:/nvtd/dsphongvan";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/nvtd/update-phongvan/" + phongVanId;
        }
    }
}
