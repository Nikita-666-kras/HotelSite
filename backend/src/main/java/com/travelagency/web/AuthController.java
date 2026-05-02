package com.travelagency.web;

import com.travelagency.dto.ChangePasswordRequest;
import com.travelagency.dto.AuthOtpChallengeResponse;
import com.travelagency.dto.LoginRequest;
import com.travelagency.dto.RequestLoginOtpRequest;
import com.travelagency.dto.RefreshRequest;
import com.travelagency.dto.RegisterRequest;
import com.travelagency.dto.TokenResponse;
import com.travelagency.dto.UpdateProfileRequest;
import com.travelagency.dto.UserResponse;
import com.travelagency.dto.VerifyLoginOtpRequest;
import com.travelagency.dto.VerifyRegisterOtpRequest;
import com.travelagency.security.UserPrincipal;
import com.travelagency.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/register/request")
    public AuthOtpChallengeResponse requestRegisterOtp(@Valid @RequestBody RegisterRequest req) {
        return authService.requestRegisterOtp(req);
    }

    @PostMapping("/register/verify")
    public TokenResponse verifyRegisterOtp(@Valid @RequestBody VerifyRegisterOtpRequest req) {
        return authService.verifyRegisterOtp(req);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/login/request")
    public AuthOtpChallengeResponse requestLoginOtp(@Valid @RequestBody RequestLoginOtpRequest req) {
        return authService.requestLoginOtp(req);
    }

    @PostMapping("/login/verify")
    public TokenResponse verifyLoginOtp(@Valid @RequestBody VerifyLoginOtpRequest req) {
        return authService.verifyLoginOtp(req);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.me(principal);
    }

    @PatchMapping("/profile")
    public UserResponse updateProfile(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody UpdateProfileRequest req) {
        return authService.updateProfile(principal, req);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(principal, req);
    }
}
