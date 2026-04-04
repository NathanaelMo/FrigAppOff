package com.frigapp.frigappapi.services;

import com.frigapp.frigappapi.model.*;
import com.frigapp.frigappapi.repositories.UserRepository;
import com.frigapp.frigappapi.security.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthRegisterPost201Response register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        String hash = passwordEncoder.encode(request.getPassword());
        UserFull user = userRepository.save(request.getFirstName(), request.getEmail(), hash);

        // Register retourne directement un UserFull (pas de token selon ton OpenAPI)
        AuthRegisterPost201Response response = new AuthRegisterPost201Response();
        response.setStatus(AuthRegisterPost201Response.StatusEnum.SUCCESS);
        response.setData(user);
        return response;
    }

    public AuthLoginPost200Response login(LoginRequest request) {
        String storedHash = userRepository.findPasswordHashByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"
                ));

        if (!passwordEncoder.matches(request.getPassword(), storedHash)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"
            );
        }

        UserFull user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user.getId());

        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setFirstName(user.getFirstName());
        summary.setEmail(user.getEmail());

        // Login retourne un AuthResponse avec le token JWT
        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        authResponse.setExpiresIn(86400);
        authResponse.setUser(summary);

        AuthLoginPost200Response response = new AuthLoginPost200Response();
        response.setStatus(AuthLoginPost200Response.StatusEnum.SUCCESS);
        response.setData(authResponse);
        return response;
    }

    public AuthLoginPost200Response refresh(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalide ou absent");
        }

        String token = authHeader.substring(7);
        UUID userId;
        try {
            userId = jwtService.extractUserIdIgnoringExpiration(token);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalide ou absent");
        }

        UserFull user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token invalide ou absent"));

        String newToken = jwtService.generateToken(user.getId());

        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setFirstName(user.getFirstName());
        summary.setEmail(user.getEmail());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(newToken);
        authResponse.setExpiresIn(86400);
        authResponse.setUser(summary);

        AuthLoginPost200Response response = new AuthLoginPost200Response();
        response.setStatus(AuthLoginPost200Response.StatusEnum.SUCCESS);
        response.setData(authResponse);
        return response;
    }
}