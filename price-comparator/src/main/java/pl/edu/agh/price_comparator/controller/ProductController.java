package pl.edu.agh.price_comparator.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import pl.edu.agh.price_comparator.entity.Product;
import pl.edu.agh.price_comparator.service.ProductService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/product")
public class ProductController
{
    private final ProductService productService;
    
    @Data
    private static class ProductDto
    {
        @JsonProperty(value = "manufacturer_code")
        private String manufacturerCode;
    }

    @GetMapping("/find/{manufacturerCode}")
    public ResponseEntity<Integer> findProduct(@PathVariable("manufacturerCode") String manufacturerCode)
    {
        return productService.findProduct(manufacturerCode);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable("id") Long id)
    {
        return productService.getProduct(id);
    }

    @PostMapping("/add")
    public ResponseEntity<Integer> addProduct(@RequestBody ProductDto request)
    {
        String manufacturerCode = request.getManufacturerCode();
        
        return productService.addProduct(manufacturerCode);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Product>> getLatestProducts()
    {
        return productService.getLatestProducts();
    }
}
