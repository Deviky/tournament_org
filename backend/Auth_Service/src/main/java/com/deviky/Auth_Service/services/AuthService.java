package com.deviky.Auth_Service.services;

import com.deviky.Auth_Service.components.EmailConfirmationTokenStore;
import com.deviky.Auth_Service.components.PasswordResetTokenStore;
import com.deviky.Auth_Service.components.TokenBlacklist;
import com.deviky.Auth_Service.dto.*;
import com.deviky.Auth_Service.models.Role;
import com.deviky.Auth_Service.models.User;
import com.deviky.Auth_Service.repositories.UserRepository;
import com.deviky.Auth_Service.security_core.SecurityUser;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.parameters.P;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklist blacklist;
    private final EmailConfirmationTokenStore emailConfirmationTokenStore;
    private final MailSender mailSender;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final ParticipantClientService participantClientService;


    @Transactional
    public <T extends LoginRequest> ApiResponse<String> register(T request, String appUrl) {
        try {
            if (userRepository.findByUsername(request.getEmail()).isPresent()) {
                return new ApiResponse<>("Пользователь с данным email уже существует", null, true);
            }

            if (!((request instanceof RegisterOrganizationRequest) || (request instanceof RegisterPlayerRequest))){
                return new ApiResponse<>("Неверная форма регистрации", null, true);
            }

            Role role = (request instanceof RegisterOrganizationRequest) ? Role.ORGANIZER : Role.PLAYER;

            User user = User.builder()
                    .username(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(role)
                    .emailConfirmed(false)
                    .build();

            User userSaved = userRepository.save(user);
            Long userId = userSaved.getId();

            if (request instanceof RegisterOrganizationRequest){
                CreateOrganizationRequest createOrganizationDto = CreateOrganizationRequest.builder()
                        .id(userId)
                        .organizerName(((RegisterOrganizationRequest) request).getOrganizerName())
                        .description(((RegisterOrganizationRequest) request).getDescription())
                        .build();
                ApiResponse<Organization> organizationApiResponse = participantClientService.createOrganizationProfile(createOrganizationDto);
                if (organizationApiResponse.isError()){
                    throw new Exception(organizationApiResponse.getMessage());
                }
            }
            else {
                CreatePlayerRequest createPlayerRequest = CreatePlayerRequest.builder()
                        .id(userId)
                        .nickname(((RegisterPlayerRequest) request).getNickname())
                        .games(((RegisterPlayerRequest) request).getGames())
                        .build();
                ApiResponse<Player> playerApiResponse = participantClientService.createPlayerProfile(createPlayerRequest);
                if (playerApiResponse.isError()){
                    throw new Exception(playerApiResponse.getMessage());
                }
            }

            sendConfirmationEmail(user.getUsername(), appUrl);

            return new ApiResponse<>("Проверьте свой email для подтверждения регистрации", null, false);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public void sendConfirmationEmail(String email, String appUrl) {
        String token = UUID.randomUUID().toString();
        emailConfirmationTokenStore.storeToken(token, email);

        String link = appUrl + "/auth/confirm?token=" + token;
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(email);
        mail.setSubject("Confirm your email");
        mail.setText("Click to confirm: " + link);
        mailSender.send(mail);
    }

    public ApiResponse<String> resendConfirmationEmail(String email, String appUrl) {
        try {
            User user = userRepository.findByUsername(email).orElseThrow(() -> new RuntimeException("User not found"));

            if (user.isEmailConfirmed()) {
                return new ApiResponse<>("Email уже подтверждён", null, true);
            }

            sendConfirmationEmail(email, appUrl);
            return new ApiResponse<>("Confirmation email resent successfully", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<AuthResponse> confirmEmailAndLogin(String token) {
        try {
            String email = emailConfirmationTokenStore.getEmail(token);
            if (email == null) return new ApiResponse<>("Token invalid or expired", null, true);

            User user = userRepository.findByUsername(email).orElseThrow();
            user.setEmailConfirmed(true);
            userRepository.save(user);

            emailConfirmationTokenStore.removeToken(token);

            String access = jwtService.generateAccessToken(user.getUsername(), user.getRole());
            String refresh = jwtService.generateRefreshToken(user.getUsername());

            return new ApiResponse<>("Email confirmed successfully", new AuthResponse(access, refresh), false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<AuthResponse> login(LoginRequest request) {
        try {
            SecurityUser securityUser = (SecurityUser) authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            ).getPrincipal();

            User user = securityUser.getUser();

            if (!user.isEmailConfirmed())
                return new ApiResponse<>("Вы не можете ", null, true);

            String access = jwtService.generateAccessToken(user.getUsername(), user.getRole());
            String refresh = jwtService.generateRefreshToken(user.getUsername());

            return new ApiResponse<>("Login successful", new AuthResponse(access, refresh), false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<AuthResponse> refresh(String refreshToken) {
        try {

            if (blacklist.isRevoked(refreshToken))
                return new ApiResponse<>("Refresh token revoked", null, true);

            Claims claims = jwtService.parseToken(refreshToken);

            String username = claims.getSubject();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isEmailConfirmed())
                return new ApiResponse<>("Email not confirmed", null, true);

            String newAccess = jwtService.generateAccessToken(user.getUsername(), user.getRole());

            return new ApiResponse<>(
                    "Token refreshed",
                    new AuthResponse(newAccess, refreshToken),
                    false
            );

        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> logout(String accessToken, String refreshToken) {
        try {

            if (accessToken != null) {
                long accessTTL = jwtService.getExpirySeconds(accessToken);
                blacklist.revoke(accessToken, accessTTL);
            }

            if (refreshToken != null) {
                long refreshTTL = jwtService.getExpirySeconds(refreshToken);
                blacklist.revoke(refreshToken, refreshTTL);
            }

            return new ApiResponse<>("Logged out successfully", null, false);

        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> sendPasswordResetEmail(String email, String appUrl) {
        try {
            User user = userRepository.findByUsername(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = UUID.randomUUID().toString();
            passwordResetTokenStore.storeToken(token, email);

            String link = appUrl + "/auth/reset_password?token=" + token;
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setSubject("Password reset");
            mail.setText("Click to reset your password: " + link);
            mailSender.send(mail);

            return new ApiResponse<>("Password reset email sent", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> resetPassword(String token, ResetPasswordRequest request) {
        try {
            String email = passwordResetTokenStore.getEmail(token);
            if (email == null) return new ApiResponse<>("Token invalid or expired", null, true);

            User user = userRepository.findByUsername(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            passwordResetTokenStore.removeToken(token);
            return new ApiResponse<>("Password reset successfully", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<AuthResponse> createModerator(ModeratorCreateRequest request) {
        try {
            User user = User.builder()
                    .username(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(Role.MODERATOR)
                    .emailConfirmed(false)
                    .build();

            userRepository.save(user);

            String access = jwtService.generateAccessToken(user.getUsername(), user.getRole());
            String refresh = jwtService.generateRefreshToken(user.getUsername());

            return new ApiResponse<>("Moderator created successfully", new AuthResponse(access, refresh), false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }
}