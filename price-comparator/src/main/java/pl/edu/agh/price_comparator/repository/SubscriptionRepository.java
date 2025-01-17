package pl.edu.agh.price_comparator.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pl.edu.agh.price_comparator.entity.Subscription;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long>
{
    List<Subscription> findByProductId(Long productId);
    List<Subscription> findByUserId(Long userId);
    Optional<Subscription> findByProductIdAndUserId(Long productId, Long userId);
}