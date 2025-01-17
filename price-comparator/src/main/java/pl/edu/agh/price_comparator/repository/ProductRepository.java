package pl.edu.agh.price_comparator.repository;

import org.springframework.stereotype.Repository;

import pl.edu.agh.price_comparator.entity.Product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>
{
    Optional<Product> findByManufacturerCode(String manufacturerCode);
    List<Product> findTop10ByOrderByCreatedAtDesc();
}
