package com.example.QLTuyenDung.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.QLTuyenDung.dto.ScoreDTO;
import com.example.QLTuyenDung.model.BaiTest;
import com.example.QLTuyenDung.model.DonUngTuyen;
import com.example.QLTuyenDung.model.KQBaiTest;
import com.example.QLTuyenDung.model.User;
import com.example.QLTuyenDung.repository.BaiTestRepository;
import com.example.QLTuyenDung.repository.DonUngTuyenRepository;
import com.example.QLTuyenDung.repository.KQBaiTestRepository;
import com.example.QLTuyenDung.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiemService {
    private final KQBaiTestRepository kqBaiTestRepository;
    private final UserRepository userRepository;
    private final BaiTestRepository baiTestRepository;
    private final DonUngTuyenRepository donUngTuyenRepository;
    
    @Transactional
    public boolean save(ScoreDTO scoreDTO) {
        try {
            log.info("Nhận kết quả test: {}", scoreDTO);
            
            if (scoreDTO.getEmail() == null || scoreDTO.getScore() == null || scoreDTO.getTitle() == null) {
                log.error("Dữ liệu không hợp lệ: {}", scoreDTO);
                return false;
            }
            
            // Tìm người dùng dựa vào email
            User user = userRepository.findByEmail(scoreDTO.getEmail());
            if (user == null) {
                log.error("Không tìm thấy người dùng với email: {}", scoreDTO.getEmail());
                return false;
            }
            
            // Tìm bài test dựa vào tiêu đề
            BaiTest baiTest = baiTestRepository.findByTieuDe(scoreDTO.getTitle());
            if (baiTest == null) {
                log.error("Không tìm thấy bài test với tiêu đề: {}", scoreDTO.getTitle());
                return false;
            }
            
            // Tìm đơn ứng tuyển của người dùng cho tin tuyển dụng liên quan đến bài test
            List<DonUngTuyen> dsUngTuyen = donUngTuyenRepository.findByUserIdAndTinTuyenDungId(
                user.getId(), baiTest.getTinTuyenDung().getId());
            
            if (dsUngTuyen == null || dsUngTuyen.isEmpty()) {
                log.error("Không tìm thấy đơn ứng tuyển của người dùng {} cho tin tuyển dụng {}", 
                    user.getUsername(), baiTest.getTinTuyenDung().getTieuDe());
                return false;
            }
            
            DonUngTuyen donUngTuyen = dsUngTuyen.get(0);
            
            // Tìm kết quả bài test của đơn ứng tuyển
            KQBaiTest kqBaiTest = kqBaiTestRepository.findByDonUngTuyenIdAndBaiTestId(
                donUngTuyen.getId(), baiTest.getId());
            
            if (kqBaiTest == null) {
                log.error("Không tìm thấy kết quả bài test cho đơn ứng tuyển ID {} và bài test ID {}", 
                    donUngTuyen.getId(), baiTest.getId());
                return false;
            }
            
            // Chuyển đổi điểm từ double sang int (làm tròn)
            int score = (int) Math.round(scoreDTO.getScore() * 100);
            
            // Cập nhật điểm và ngày làm bài
            kqBaiTest.setDiem(score);
            if (scoreDTO.getSubmittedAt() != null && !scoreDTO.getSubmittedAt().isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Date submittedDate = dateFormat.parse(scoreDTO.getSubmittedAt());
                    kqBaiTest.setNgayLam(submittedDate);
                } catch (ParseException e) {
                    log.error("Lỗi khi chuyển đổi ngày từ chuỗi: {}", scoreDTO.getSubmittedAt(), e);
                    kqBaiTest.setNgayLam(new Date()); // Nếu không parse được thì dùng ngày hiện tại
                }
            } else {
                kqBaiTest.setNgayLam(new Date());
            }
            
            // Lưu kết quả
            kqBaiTestRepository.save(kqBaiTest);
            log.info("Đã cập nhật kết quả bài test thành công: ID={}, điểm={}", kqBaiTest.getId(), score);
            
            return true;
        } catch (Exception e) {
            log.error("Lỗi khi lưu kết quả bài test", e);
            return false;
        }
    }
}