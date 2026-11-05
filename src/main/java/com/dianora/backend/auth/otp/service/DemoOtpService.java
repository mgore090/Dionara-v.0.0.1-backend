package com.dianora.backend.auth.otp.service;

import com.dianora.backend.auth.otp.dto.*;
import com.dianora.backend.user.entity.User;
import com.dianora.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DemoOtpService {
    private static final String DEMO_OTP = "123456";
    private final UserRepository userRepository;

    public DemoOtpService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String sendOtp(SendOtpRequest request) {
        normalizePhone(request.phone());
        // Development only: no SMS is sent.
        return "Demo OTP generated successfully. Use 123456.";
    }

    @Transactional
    public OtpUserResponse verifyOtp(VerifyOtpRequest request) {
        String phone = normalizePhone(request.phone());
        if (!DEMO_OTP.equals(request.otp())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid OTP. For demo, enter 123456."
            );
        }

        User user = userRepository.findFirstByPhone(phone).orElse(null);
        boolean newUser = false;

        if (user == null) {
            user = userRepository.save(new User(null, null, phone));
            newUser = true;
        } else if (!user.isActive()) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN, "This customer account is inactive."
            );
        }

        return new OtpUserResponse(
            user.getId(), user.getFullName(), user.getEmail(),
            user.getPhone(), user.isActive(), newUser, user.getCreatedAt()
        );
    }

    private String normalizePhone(String phone) {
        String value = phone == null ? "" : phone.trim().replaceAll("\\s+", "");
        if (!value.matches("^[6-9][0-9]{9}$")) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Enter a valid 10-digit Indian mobile number."
            );
        }
        return value;
    }
}
