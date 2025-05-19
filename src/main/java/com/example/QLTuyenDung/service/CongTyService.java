package com.example.QLTuyenDung.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.QLTuyenDung.model.CongTy;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.repository.CongTyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CongTyService {
    private final CongTyRepository congTyRepository;

    private final FileStorageService fileStorageService;
    private final UserService userService;
    
    public List<CongTy> getAllCongTy() {
        return congTyRepository.findAll();
    }

    public CongTy getCongTyById(Long id) {
        return congTyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy công ty!"));
    }

    public CongTy addCongTy(CongTy congTy) {
        if (congTy.getTenCongTy() == null || congTy.getTenCongTy().trim().isEmpty()) {
            throw new RuntimeException("Tên công ty không được để trống!");
        }
        return congTyRepository.save(congTy);
    }

    public CongTy updateCongTy(CongTy congTy) {
        if (!congTyRepository.existsById(congTy.getId())) {
            throw new RuntimeException("Không tìm thấy công ty!");
        }
        return congTyRepository.save(congTy);
    }

    public void deleteCongTy(Long id) {
        if (!congTyRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy công ty!");
        }
        congTyRepository.deleteById(id);
    }

    @Transactional
    public CongTy getCongTyForCurrentUser(User user) {
        if (user.getCongTy() == null) {
            return new CongTy();
        }
        // Sử dụng congTyService để load lại toàn bộ thông tin công ty (refresh)
        return getCongTyById(user.getCongTy().getId());
    }

    @Transactional
    public void updateThongTinCongTy(User user, CongTy congTyRequest, MultipartFile logoFile) {
        // Xử lý upload logo nếu có
        if (logoFile != null && !logoFile.isEmpty()) {
            String fileName = fileStorageService.storeFile(logoFile);
            congTyRequest.setLogo(fileName);
        }
        
        if (user.getCongTy() == null) {
            // Tạo mới công ty
            CongTy congTyMoi = addCongTy(congTyRequest);
            user.setCongTy(congTyMoi);
            userService.updateUser(user);
        } else {
            // Cập nhật công ty hiện có
            CongTy congTyHienTai = getCongTyById(user.getCongTy().getId());
            congTyHienTai.setTenCongTy(congTyRequest.getTenCongTy());
            congTyHienTai.setEmail(congTyRequest.getEmail());
            congTyHienTai.setWebsite(congTyRequest.getWebsite());
            congTyHienTai.setHotLine(congTyRequest.getHotLine());
            congTyHienTai.setNamThanhLap(congTyRequest.getNamThanhLap());
            congTyHienTai.setQuyMo(congTyRequest.getQuyMo());
            congTyHienTai.setLinhVuc(congTyRequest.getLinhVuc());
            congTyHienTai.setMoTa(congTyRequest.getMoTa());
            
            if (congTyRequest.getLogo() != null) {
                congTyHienTai.setLogo(congTyRequest.getLogo());
            }
            
            updateCongTy(congTyHienTai);
        }
    }

    @Transactional
    public void updateDiaChiCongTy(User user, String diaChi) {
        if (user.getCongTy() == null) {
            throw new IllegalStateException("Vui lòng cập nhật thông tin công ty trước!");
        }
        
        CongTy congTyHienTai = getCongTyById(user.getCongTy().getId());
        congTyHienTai.setDiaChi(diaChi);
        updateCongTy(congTyHienTai);
    }

    @Transactional
    public void updateMXHCongTy(User user, String facebook, String linkedIn) {
        if (user.getCongTy() == null) {
            throw new IllegalStateException("Vui lòng cập nhật thông tin công ty trước!");
        }
        
        CongTy congTyHienTai = getCongTyById(user.getCongTy().getId());
        congTyHienTai.setFacebook(facebook);
        congTyHienTai.setLinkedIn(linkedIn);
        updateCongTy(congTyHienTai);
    }
}
