package com.example.QLTuyenDung.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QLTuyenDung.model.BaiTest;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.KQBaiTest;
import com.example.QLTuyenDung.model.PhongVan;
import com.example.QLTuyenDung.repository.DonUngTuyenRepository;
import com.example.QLTuyenDung.repository.KQBaiTestRepository;
import com.example.QLTuyenDung.repository.PhongVanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KQBaiTestService {
    private final KQBaiTestRepository kqBaiTestRepository;
    private final DonUngTuyenRepository donUngTuyenRepository;
    private final DonUngTuyenService donUngTuyenService;
    private final BaiTestService baiTestService;

    public List<KQBaiTest> getAllKQBaiTest() {
        return kqBaiTestRepository.findAll();
    }

    public List<DonUngTuyen> filterKetQuaBaiTest(
            Integer diemNgonNgu, 
            Integer diemLogic, 
            Integer diemChuyenMon) {
        
        List<DonUngTuyen> allDons = donUngTuyenRepository.findAll();
        
        return allDons.stream()
            .filter(don -> isMatchFilter(don, "ngonngu", diemNgonNgu))
            .filter(don -> isMatchFilter(don, "logic", diemLogic))
            .filter(don -> isMatchFilter(don, "chuyenmon", diemChuyenMon))
            .collect(Collectors.toList());
    }

    private boolean isMatchFilter(DonUngTuyen don, String loaiTest, Integer diemMin) {
        if (diemMin == null) {
            return true; // Skip filter if not specified
        }

        return don.getDSKQBaiTest().stream()
            .filter(kq -> kq.getBaiTest().getLoai().equals(loaiTest))
            .mapToInt(KQBaiTest::getDiem)
            .allMatch(diem -> diem >= diemMin);
    }

    public KQBaiTest addKQBaiTest(KQBaiTest kqBaiTest) {
        if (kqBaiTest.getDonUngTuyen() == null || kqBaiTest.getDonUngTuyen().getId() == null) {
            throw new RuntimeException("Vui lòng chọn đơn ứng tuyển!");
        }

        if (kqBaiTest.getBaiTest() == null || kqBaiTest.getBaiTest().getId() == null) {
            throw new RuntimeException("Vui lòng chọn bài test!");
        }

        if (kqBaiTest.getDiem() < 0 || kqBaiTest.getDiem() > 100) {
            throw new RuntimeException("Điểm phải từ 0 đến 100!");
        }

        if (kqBaiTest.getNgayLam() == null) {
            throw new RuntimeException("Vui lòng chọn ngày làm bài!");
        }

        boolean exists = kqBaiTestRepository.existsByDonUngTuyenIdAndBaiTestId(
            kqBaiTest.getDonUngTuyen().getId(),
            kqBaiTest.getBaiTest().getId()
        );
        if (exists) {
            throw new RuntimeException("Đã tồn tại kết quả bài test này cho ứng viên!");
        }

        return kqBaiTestRepository.save(kqBaiTest);
    }

    public KQBaiTest getKQBaiTestById(Long id) {
        return kqBaiTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả bài test!"));
    }

    public KQBaiTest updateKQBaiTest(KQBaiTest kqBaiTest) {
        if (!kqBaiTestRepository.existsById(kqBaiTest.getId())) {
            throw new RuntimeException("Không tìm thấy kết quả bài test!");
        }
        
        if (kqBaiTest.getDiem() < 0 || kqBaiTest.getDiem() > 100) {
            throw new RuntimeException("Điểm phải từ 0 đến 100!");
        }
        return kqBaiTestRepository.save(kqBaiTest);
    }

    // Thêm phương thức xóa quyền truy cập bài test
    @Transactional
    public void deleteKQBaiTest(Long kqBaiTestId) {
        KQBaiTest kqBaiTest = kqBaiTestRepository.findById(kqBaiTestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả bài test!"));
        
        // Cập nhật đơn ứng tuyển để hủy quyền test nếu cần
        DonUngTuyen donUngTuyen = kqBaiTest.getDonUngTuyen();
        
        // Đếm số lượng bài test mà đơn ứng tuyển này còn lại
        long countRemaining = kqBaiTestRepository.countByDonUngTuyenIdAndIdNot(donUngTuyen.getId(), kqBaiTestId);
        
        // Nếu không còn bài test nào, hủy quyền test
        if (countRemaining == 0) {
            donUngTuyen.setQuyenTest(false);
            donUngTuyenRepository.save(donUngTuyen);
        }
        
        // Xóa kết quả bài test
        kqBaiTestRepository.delete(kqBaiTest);
    }

    @Transactional
    public void duyetPhongVan(List<Long> donIds) {
        if (donIds == null || donIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một ứng viên nếu bạn muốn duyệt phỏng vấn!");
        }

        List<DonUngTuyen> donDaChon = donUngTuyenRepository.findAllById(donIds);
    
        if (donDaChon.isEmpty()) {
            throw new RuntimeException("Không tìm thấy đơn ứng tuyển nào với các ID đã chọn!");
        }
    
        for (DonUngTuyen don : donDaChon) {
            // Chỉ cập nhật nếu đơn chưa ở trạng thái phỏng vấn
            if (!"phongvan".equals(don.getTrangThai())) {
                don.setTrangThai("phongvan");
                donUngTuyenService.kiemTraTrangThaiPV(don);
                donUngTuyenRepository.save(don);
            }
        }
    }

    public List<KQBaiTest> getKQBaiTestByBaiTestId(Long baiTestId) {
        return kqBaiTestRepository.findByBaiTestId(baiTestId);
    }

    @Transactional
    public int themQuyenTruyCapBaiTest(List<Long> donIds, Long baiTestId) {
        if (donIds == null || donIds.isEmpty()) {
            throw new RuntimeException("Danh sách đơn ứng tuyển không được để trống!");
        }
        
        BaiTest baiTest = baiTestService.getBaiTestById(baiTestId);
        
        List<DonUngTuyen> dsDonUngTuyen = donUngTuyenRepository.findAllById(donIds);
        
        if (dsDonUngTuyen.size() != donIds.size()) {
            throw new RuntimeException("Một số đơn ứng tuyển không tồn tại!");
        }
        
        // Kiểm tra tất cả đơn có trạng thái "chotest" và thuộc tin tuyển dụng của bài test
        for (DonUngTuyen don : dsDonUngTuyen) {
            if (!"chotest".equals(don.getTrangThai())) {
                throw new RuntimeException("Đơn ứng tuyển ID " + don.getId() + " không ở trạng thái chờ bài test!");
            }
            
            if (!don.getTinTuyenDung().getId().equals(baiTest.getTinTuyenDung().getId())) {
                throw new RuntimeException("Đơn ứng tuyển ID " + don.getId() + " không thuộc tin tuyển dụng của bài test!");
            }
        }
        
        // Kiểm tra đơn đã có kết quả bài test này chưa
        List<KQBaiTest> dsTruyCap = kqBaiTestRepository.findByBaiTestId(baiTestId);
        Set<Long> donDaCoTruyCap = dsTruyCap.stream()
            .map(kq -> kq.getDonUngTuyen().getId())
            .collect(Collectors.toSet());
        
        List<DonUngTuyen> dsDonChuaCoTruyCap = dsDonUngTuyen.stream()
            .filter(don -> !donDaCoTruyCap.contains(don.getId()))
            .collect(Collectors.toList());
        
        if (dsDonChuaCoTruyCap.isEmpty()) {
            throw new RuntimeException("Tất cả đơn ứng tuyển đã được cấp quyền truy cập bài test!");
        }
        
        // Tạo kết quả bài test mới cho các đơn ứng tuyển
        List<KQBaiTest> dsKQBaiTestMoi = new ArrayList<>();
        for (DonUngTuyen don : dsDonChuaCoTruyCap) {
            KQBaiTest kqBaiTest = new KQBaiTest();
            don.setQuyenTest(true);
            kqBaiTest.setDonUngTuyen(don);
            kqBaiTest.setBaiTest(baiTest);
            kqBaiTest.setDiem(0); // Điểm mặc định là 0
            dsKQBaiTestMoi.add(kqBaiTest);
        }
        
        // Lưu các kết quả bài test mới
        donUngTuyenRepository.saveAll(dsDonChuaCoTruyCap);
        kqBaiTestRepository.saveAll(dsKQBaiTestMoi);
        return dsKQBaiTestMoi.size();
    }

    public List<KQBaiTest> getKQBaiTestsByDonUngTuyenId(Long donUngTuyenId) {
        return kqBaiTestRepository.findByDonUngTuyenId(donUngTuyenId);
    }
}
