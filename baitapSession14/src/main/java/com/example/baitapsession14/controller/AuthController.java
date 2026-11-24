package com.example.baitapsession14.controller;

import com.example.baitapsession14.dto.LoginRequest;
import com.example.baitapsession14.dto.LoginResponse;
import com.example.baitapsession14.security.JwtProvider;
import com.example.baitapsession14.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        var principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtProvider.generateToken(principal);
        return ResponseEntity.ok(new LoginResponse(token, "Bearer", principal.getUsername()));
    }

    @RequestMapping(value = "/test", method = {RequestMethod.GET, RequestMethod.POST})
    public Map<String, String> test() {
        return Map.of("message", "API public truy cập thành công");
    }
}
