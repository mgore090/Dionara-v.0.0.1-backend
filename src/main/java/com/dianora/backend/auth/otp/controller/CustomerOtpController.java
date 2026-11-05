package com.dianora.backend.auth.otp.controller;

import com.dianora.backend.auth.otp.dto.*;
import com.dianora.backend.auth.otp.service.DemoOtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/otp")
public class CustomerOtpController {
    private final DemoOtpService demoOtpService;

    public CustomerOtpController(DemoOtpService demoOtpService) {
        this.demoOtpService = demoOtpService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendOtp(
        @Valid @RequestBody SendOtpRequest request
    ) {
        return ResponseEntity.ok(Map.of(
            "message", demoOtpService.sendOtp(request)
        ));
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpUserResponse> verifyOtp(
        @Valid @RequestBody VerifyOtpRequest request
    ) {
        return ResponseEntity.ok(demoOtpService.verifyOtp(request));
    }
}
