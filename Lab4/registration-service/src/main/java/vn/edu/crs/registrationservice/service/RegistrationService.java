package vn.edu.crs.registrationservice.service;

import vn.edu.crs.registrationservice.client.CourseClient;
import vn.edu.crs.registrationservice.dto.RegistrationRequestDTO;
import vn.edu.crs.registrationservice.entity.Registration;
import vn.edu.crs.registrationservice.repository.RegistrationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private static final String DA_DANG_KY = "DA_DANG_KY";
    private static final String DA_HUY = "DA_HUY";

    private final RegistrationRepository registrationRepository;
    private final CourseClient courseClient;

    public Registration register(RegistrationRequestDTO dto) {

        // Kiểm tra sinh viên đã đăng ký môn này chưa
        if (registrationRepository.existsByStudentIdAndCourseIdAndTrangThai(
                dto.getStudentId(),
                dto.getCourseId(),
                DA_DANG_KY)) {

            throw new IllegalStateException(
                    "Sinh vien da dang ky mon hoc nay roi"
            );
        }

        // Bước 1: gọi course-service để trừ chỗ TRƯỚC
        // Nếu course-service báo lỗi thì dừng lại,
        // không lưu Registration.
        courseClient.reserveSeat(dto.getCourseId());

        // Bước 2: course-service xác nhận thành công
        // mới tạo Registration
        Registration registration = new Registration();

        registration.setStudentId(dto.getStudentId());
        registration.setCourseId(dto.getCourseId());
        registration.setTrangThai(DA_DANG_KY);
        registration.setNgayDangKy(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    public void cancel(Long registrationId) {

        // Tìm bản đăng ký
        Registration registration =
                registrationRepository.findById(registrationId)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Khong tim thay dang ky id = "
                                                + registrationId
                                )
                        );

        // Nếu đã hủy rồi thì không được hủy tiếp
        if (DA_HUY.equals(registration.getTrangThai())) {

            throw new IllegalStateException(
                    "Dang ky nay da duoc huy truoc do"
            );
        }

        // Gọi course-service hoàn chỗ TRƯỚC
        courseClient.releaseSeat(registration.getCourseId());

        // Sau khi hoàn chỗ thành công
        // mới chuyển trạng thái đăng ký thành DA_HUY
        registration.setTrangThai(DA_HUY);

        registrationRepository.save(registration);
    }
}