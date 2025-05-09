package com.example.QLTuyenDung.controller.admin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.service.DonUngTuyenService;
import com.example.QLTuyenDung.service.PhongVanService;
import com.example.QLTuyenDung.service.UserService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class QLPhongVanController {
    private final PhongVanService phongVanService;
    private final UserService userService;
    private final DonUngTuyenService donUngTuyenService;

    @GetMapping("/dsphongvan")
    public String showDSPhongVan(Model model) {
        List<PhongVan> dsPhongVan = phongVanService.getAllPhongVan();
        
        // Group by donUngTuyen.id
        Map<Long, List<PhongVan>> groupedByDon = dsPhongVan.stream()
        .collect(Collectors.groupingBy(pv -> pv.getDonUngTuyen().getId()));
    
        model.addAttribute("groupedByDon", groupedByDon);
        return "admin/QLQuyTrinhTuyenDung/QLPhongVan/index";
    }

    @GetMapping("/get-nhanvien-by-congty")
    @ResponseBody
    public List<User> getNhanVienByCongTy(@RequestParam Long tinTuyenDungId, @RequestParam Long donUngTuyenId) {
        try {
            System.out.println("tinTuyenDungId: " + tinTuyenDungId);
            List<User> allStaff = userService.getNhanVienTuyenDungByCongTy(tinTuyenDungId);

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

    @PostMapping("/phancongpv")
    public String phanCongPhongVan(@RequestParam Long phongVanId,
                            @RequestParam Long tinTuyenDungId,
                            @RequestParam List<Long> nhanVienIds,
                            RedirectAttributes redirectAttributes) {
        try {
            // Validate that selected staff belong to the correct company
            List<User> dSNVTDCuaCongTy = userService.getNhanVienTuyenDungByCongTy(tinTuyenDungId);
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
    @GetMapping("/delete-nvtd/{pvId}/{nvId}")
    public String deleteNVTDCuaPhongVan(@PathVariable Long pvId, 
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

    @GetMapping("/chitiet-phongvan/{id}")
    public String chiTietPhongVan(@PathVariable Long id, Model model) {
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

    
    @PostMapping("/capnhat-lich-phongvan")
    public String capNhatLichPhongVan(
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

    @PostMapping("/danh-gia-pv")
    public String danhGiaPhongVan(
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
}
