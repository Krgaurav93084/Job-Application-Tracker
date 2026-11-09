package com.kumargaurav.jobtracker.service;

import com.kumargaurav.jobtracker.dto.Dtos.AuthRequest;
import com.kumargaurav.jobtracker.dto.Dtos.AuthResponse;
import com.kumargaurav.jobtracker.exception.ConflictException;
import com.kumargaurav.jobtracker.model.User;
import com.kumargaurav.jobtracker.repository.UserRepository;
import com.kumargaurav.jobtracker.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository users,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(AuthRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email is already registered");
        }
        users.save(new User(email, passwordEncoder.encode(request.password())));
        return new AuthResponse(jwtService.generateToken(email));
    }

    public AuthResponse login(AuthRequest request) {
        String email = request.email().trim().toLowerCase();
        // Throws BadCredentialsException (-> HTTP 401) when email/password do not match.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        return new AuthResponse(jwtService.generateToken(email));
    }
}
