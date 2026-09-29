package com.example.foodshare.controller;

import com.example.foodshare.entity.AccountRole;
import com.example.foodshare.entity.FoodShareAccount;
import com.example.foodshare.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken(), "headerName", csrfToken.getHeaderName());
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody SignupRequest request) {
        FoodShareAccount account = accountService.signUp(
                request.name(), request.email(), request.phone(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "email", account.getEmail(),
                "role", account.getRole().name(),
                "message", "Account created. Sign in to continue."));
    }

    @GetMapping("/me")
    public Map<String, String> currentUser(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("USER");
        return Map.of("email", authentication.getName(), "role", role);
    }

    public record SignupRequest(
            @NotBlank(message = "Name is required") String name,
            @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
            @NotBlank(message = "Phone is required") String phone,
            @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must have at least 8 characters") String password,
            @NotNull(message = "Choose Donor or NGO") AccountRole role) {
    }
}