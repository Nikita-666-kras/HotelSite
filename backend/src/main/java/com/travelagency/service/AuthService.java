package com.travelagency.service;

import com.travelagency.domain.Role;
import com.travelagency.domain.User;
import com.travelagency.domain.EmailOtpChallenge;
import com.travelagency.domain.EmailOtpPurpose;
import com.travelagency.dto.AuthOtpChallengeResponse;
import com.travelagency.dto.ChangePasswordRequest;
import com.travelagency.dto.LoginRequest;
import com.travelagency.dto.RequestLoginOtpRequest;
import com.travelagency.dto.RegisterRequest;
import com.travelagency.dto.UpdateProfileRequest;
import com.travelagency.dto.TokenResponse;
import com.travelagency.dto.UserResponse;
import com.travelagency.dto.VerifyLoginOtpRequest;
import com.travelagency.dto.VerifyRegisterOtpRequest;
import com.travelagency.repository.EmailOtpChallengeRepository;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.JwtService;
import com.travelagency.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailOtpChallengeRepository emailOtpChallengeRepository;
    private final ResendEmailService resendEmailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            EmailOtpChallengeRepository emailOtpChallengeRepository,
            ResendEmailService resendEmailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailOtpChallengeRepository = emailOtpChallengeRepository;
        this.resendEmailService = resendEmailService;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        throw new BadRequestException("Use /api/auth/register/request and /api/auth/register/verify");
    }

    public TokenResponse login(LoginRequest req) {
        throw new BadRequestException("Use /api/auth/login/request and /api/auth/login/verify");
    }

    @Transactional
    public AuthOtpChallengeResponse requestRegisterOtp(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already registered");
        }
        ensureResendConfigured();
        String code = nextCode();
        EmailOtpChallenge challenge = new EmailOtpChallenge();
        challenge.setEmail(email);
        challenge.setPurpose(EmailOtpPurpose.REGISTER);
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setExpiresAt(Instant.now().plusSeconds(10 * 60));
        challenge.setRegisterPasswordHash(passwordEncoder.encode(req.password()));
        challenge.setRegisterFullName(req.fullName().trim());
        challenge.setRegisterPhone(req.phone() == null || req.phone().isBlank() ? null : req.phone().trim());
        emailOtpChallengeRepository.deleteByEmailAndPurpose(email, EmailOtpPurpose.REGISTER);
        emailOtpChallengeRepository.save(challenge);
        resendEmailService.sendPlain(
                email,
                "TripWave: код подтверждения регистрации",
                "Ваш код: " + code + "\nКод действует 10 минут.");
        return new AuthOtpChallengeResponse(email, EmailOtpPurpose.REGISTER.name(), 600);
    }

    @Transactional
    public TokenResponse verifyRegisterOtp(VerifyRegisterOtpRequest req) {
        String email = req.email().trim().toLowerCase();
        EmailOtpChallenge challenge = getActiveChallenge(email, EmailOtpPurpose.REGISTER);
        if (!passwordEncoder.matches(req.code(), challenge.getCodeHash())) {
            throw new BadRequestException("Invalid verification code");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BadRequestException("Email already registered");
        }
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(challenge.getRegisterPasswordHash());
        u.setFullName(challenge.getRegisterFullName());
        u.setPhone(challenge.getRegisterPhone());
        u.setRole(Role.USER);
        userRepository.save(u);
        challenge.setConsumedAt(Instant.now());
        emailOtpChallengeRepository.save(challenge);
        return issueTokens(u);
    }

    @Transactional
    public AuthOtpChallengeResponse requestLoginOtp(RequestLoginOtpRequest req) {
        ensureResendConfigured();
        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password()));
        } catch (AuthenticationException e) {
            throw new BadRequestException("Invalid credentials");
        }
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User u =
                userRepository
                        .findByEmailIgnoreCase(principal.getUsername())
                        .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        String email = u.getEmail().toLowerCase();
        String code = nextCode();
        EmailOtpChallenge challenge = new EmailOtpChallenge();
        challenge.setEmail(email);
        challenge.setPurpose(EmailOtpPurpose.LOGIN);
        challenge.setCodeHash(passwordEncoder.encode(code));
        challenge.setExpiresAt(Instant.now().plusSeconds(10 * 60));
        challenge.setLoginUser(u);
        emailOtpChallengeRepository.deleteByEmailAndPurpose(email, EmailOtpPurpose.LOGIN);
        emailOtpChallengeRepository.save(challenge);
        resendEmailService.sendPlain(
                email,
                "TripWave: код входа",
                "Ваш код для входа: " + code + "\nКод действует 10 минут.");
        return new AuthOtpChallengeResponse(email, EmailOtpPurpose.LOGIN.name(), 600);
    }

    @Transactional
    public TokenResponse verifyLoginOtp(VerifyLoginOtpRequest req) {
        String email = req.email().trim().toLowerCase();
        EmailOtpChallenge challenge = getActiveChallenge(email, EmailOtpPurpose.LOGIN);
        if (!passwordEncoder.matches(req.code(), challenge.getCodeHash())) {
            throw new BadRequestException("Invalid verification code");
        }
        if (challenge.getLoginUser() == null) {
            throw new BadRequestException("Login challenge is corrupted");
        }
        User user = userRepository
                .findById(challenge.getLoginUser().getId())
                .orElseThrow(() -> new BadRequestException("User not found"));
        challenge.setConsumedAt(Instant.now());
        emailOtpChallengeRepository.save(challenge);
        return issueTokens(user);
    }

    public TokenResponse refresh(String refreshToken) {
        Claims claims = jwtService.parseAndValidate(refreshToken);
        if (!jwtService.isRefreshToken(claims)) {
            throw new BadRequestException("Invalid refresh token");
        }
        UUID userId = UUID.fromString(claims.getSubject());
        User u = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("User not found"));
        String access = jwtService.createAccessToken(u.getId(), u.getEmail(), u.getRole());
        String refresh = jwtService.createRefreshToken(u.getId());
        long expSec = jwtService.parseAndValidate(access).getExpiration().getTime() / 1000
                - System.currentTimeMillis() / 1000;
        return new TokenResponse(access, refresh, "Bearer", Math.max(expSec, 0));
    }

    @Transactional(readOnly = true)
    public UserResponse me(UserPrincipal principal) {
        User u =
                userRepository
                        .findById(principal.getId())
                        .orElseThrow(() -> new NotFoundException("User not found"));
        return toUserResponse(u);
    }

    @Transactional
    public UserResponse updateProfile(UserPrincipal principal, UpdateProfileRequest req) {
        User u =
                userRepository
                        .findById(principal.getId())
                        .orElseThrow(() -> new NotFoundException("User not found"));
        if (req.fullName() != null) {
            String fn = req.fullName().trim();
            if (fn.isEmpty()) {
                throw new BadRequestException("Full name cannot be empty");
            }
            u.setFullName(fn);
        }
        if (req.phone() != null) {
            u.setPhone(req.phone().isBlank() ? null : req.phone().trim());
        }
        userRepository.save(u);
        return toUserResponse(u);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest req) {
        User u =
                userRepository
                        .findById(principal.getId())
                        .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(req.currentPassword(), u.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        u.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(u);
    }

    private static UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole());
    }

    private void ensureResendConfigured() {
        if (!resendEmailService.isAvailable()) {
            throw new BadRequestException("Email verification is unavailable: configure app.resend.api-key and app.mail.from");
        }
    }

    private String nextCode() {
        int n = secureRandom.nextInt(900_000) + 100_000;
        return String.valueOf(n);
    }

    private EmailOtpChallenge getActiveChallenge(String email, EmailOtpPurpose purpose) {
        EmailOtpChallenge challenge = emailOtpChallengeRepository
                .findTopByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new BadRequestException("Verification code was not requested"));
        if (challenge.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification code has expired");
        }
        return challenge;
    }

    private TokenResponse issueTokens(User u) {
        String access = jwtService.createAccessToken(u.getId(), u.getEmail(), u.getRole());
        String refresh = jwtService.createRefreshToken(u.getId());
        long expSec = jwtService.parseAndValidate(access).getExpiration().getTime() / 1000
                - System.currentTimeMillis() / 1000;
        return new TokenResponse(access, refresh, "Bearer", Math.max(expSec, 0));
    }
}
