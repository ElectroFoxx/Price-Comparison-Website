package pl.edu.agh.price_comparator.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.agh.price_comparator.dto.AuthResponse;
import pl.edu.agh.price_comparator.entity.User;
import pl.edu.agh.price_comparator.service.AuthService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/auth")
public class AuthController
{
    private final AuthService authService;

    @Data
    private static class UserDto
    {
        private String username;
        private String password;
        private String email;
    }
    
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<AuthResponse> register(@RequestBody User request)
    {
        return authService.register(request);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
    public ResponseEntity<AuthResponse> login(@RequestBody UserDto request)
    {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());

        return authService.authenticate(user);
    }

    @GetMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response)
    {
        return authService.refresh(request, response);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> check(HttpServletRequest request, HttpServletResponse response)
    {
        if (authService.checkToken(request, response))
            return ResponseEntity.ok(true);
        else
            return ResponseEntity.ok(false);
    }
}
