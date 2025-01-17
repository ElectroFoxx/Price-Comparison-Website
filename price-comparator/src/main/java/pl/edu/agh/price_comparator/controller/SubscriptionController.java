package pl.edu.agh.price_comparator.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.agh.price_comparator.entity.Product;
import pl.edu.agh.price_comparator.entity.User;
import pl.edu.agh.price_comparator.repository.ProductRepository;
import pl.edu.agh.price_comparator.repository.UserRepository;
import pl.edu.agh.price_comparator.service.JwtService;
import pl.edu.agh.price_comparator.service.SubscriptionService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/subscription")
public class SubscriptionController
{
    private final SubscriptionService subscriptionService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Data
    private static class SubscribeDto
    {
        private Integer price;
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> subscribe(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id, @RequestBody SubscribeDto requestBody)
    {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        Product product = productRepository.findById(id).orElseThrow();

        return subscriptionService.subscribe(user, product, requestBody.getPrice());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> unsubscribe(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id)
    {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        Product product = productRepository.findById(id).orElseThrow();

        return subscriptionService.unsubscribe(user, product);
    }
}