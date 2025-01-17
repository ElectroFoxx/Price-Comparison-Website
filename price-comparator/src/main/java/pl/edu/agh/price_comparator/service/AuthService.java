package pl.edu.agh.price_comparator.service;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import pl.edu.agh.price_comparator.dto.AuthResponse;
import pl.edu.agh.price_comparator.entity.User;
import pl.edu.agh.price_comparator.repository.UserRepository;

@AllArgsConstructor
@Service
public class AuthService
{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public ResponseEntity<AuthResponse> register(User request)
    {
        if (userRepository.findByUsername(request.getUsername()).isPresent())
        {
            return ResponseEntity
            .badRequest()
            .body(new AuthResponse(null, null, "Username already exists"));
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent())
        {
            return ResponseEntity
            .badRequest()
            .body(new AuthResponse(null, null, "Email already exists"));
        }

        request.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(request);

        String accessToken = jwtService.generateAccessToken(request);
        String refreshToken = jwtService.generateRefreshToken(request);

        return ResponseEntity
        .ok()
        .body(new AuthResponse(accessToken, refreshToken, "User registered successfully"));
    }
    
    public ResponseEntity<AuthResponse> authenticate(User request)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if (user.isEmpty())
            return ResponseEntity
            .badRequest()
            .body(new AuthResponse(null, null, "User not found"));

        String accessToken = jwtService.generateAccessToken(user.get());
        String refreshToken = jwtService.generateRefreshToken(user.get());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, "User authenticated successfully"));
    }

    public Boolean checkToken(HttpServletRequest request, HttpServletResponse response)
    {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(authHeader == null || !authHeader.startsWith("Bearer "))
        {
            return false;
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();

        return jwtService.isTokenValid(token, user);
}

    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refresh'");
    }

}
