package pl.edu.agh.price_comparator.service;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import lombok.AllArgsConstructor;
import pl.edu.agh.price_comparator.entity.Product;
import pl.edu.agh.price_comparator.entity.Subscription;
import pl.edu.agh.price_comparator.entity.User;
import pl.edu.agh.price_comparator.repository.SubscriptionRepository;

@Service
@AllArgsConstructor
public class SubscriptionService
{
    private final SubscriptionRepository subscriptionRepository;

    public ResponseEntity<String> subscribe(User user, Product product, Integer price)
    {
        Optional<Subscription> checkSubscription = subscriptionRepository.findByProductIdAndUserId(user.getId(), product.getId());

        if (checkSubscription.isPresent())
            return ResponseEntity.badRequest().body("Already subscribed");

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setProduct(product);
        subscription.setPrice(price);
        subscriptionRepository.save(subscription);

        return ResponseEntity.ok("OK");
    }

    public ResponseEntity<String> unsubscribe(User user, Product product)
    {
        Subscription subscription = subscriptionRepository.findByProductIdAndUserId(product.getId(), user.getId()).orElseThrow();
        subscriptionRepository.delete(subscription);

        return ResponseEntity.ok("OK");
    }
}