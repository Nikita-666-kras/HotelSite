package com.travelagency.service;

import com.travelagency.domain.Role;
import com.travelagency.domain.User;
import com.travelagency.dto.LoginRequest;
import com.travelagency.dto.RegisterRequest;
import com.travelagency.dto.TokenResponse;
import com.travelagency.dto.UserResponse;
import com.travelagency.repository.UserRepository;
import com.travelagency.security.JwtService;
import com.travelagency.security.UserPrincipal;
import io.jsonwebtoken.Claims;
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

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new BadRequestException("Email already registered");
        }
        User u = new User();
        u.setEmail(req.email().trim().toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setFullName(req.fullName().trim());
        u.setPhone(req.phone() != null ? req.phone().trim() : null);
        u.setRole(Role.USER);
        userRepository.save(u);
        return toUserResponse(u);
    }

    public TokenResponse login(LoginRequest req) {
        Authentication auth =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.password()));
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        User u =
                userRepository
                        .findByEmailIgnoreCase(principal.getUsername())
                        .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        String access = jwtService.createAccessToken(u.getId(), u.getEmail(), u.getRole());
        String refresh = jwtService.createRefreshToken(u.getId());
        long expSec = jwtService.parseAndValidate(access).getExpiration().getTime() / 1000
                - System.currentTimeMillis() / 1000;
        return new TokenResponse(access, refresh, "Bearer", Math.max(expSec, 0));
    }

    public TokenResponse refresh(String refreshToken) {
        Claims claims = jwtService.parseAndValidate(refreshToken);
        if (!jwtService.isRefreshToken(claims)) {
            throw new BadRequestException("Invalid refresh token");
        }
        Long userId = Long.parseLong(claims.getSubject());
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

    private static UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole());
    }
}
