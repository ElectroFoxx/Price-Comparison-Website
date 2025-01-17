package pl.edu.agh.price_comparator.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.price_comparator.entity.Product;
import pl.edu.agh.price_comparator.repository.ProductRepository;

@Service
public class ProductService
{
    private final ProductRepository productRepository;
    private final WebClient webClient;

    public ProductService(ProductRepository productRepository, WebClient.Builder webClientBuilder)
    {
        this.productRepository = productRepository;
        this.webClient = webClientBuilder.baseUrl("https://scrape-1036168743553.europe-central2.run.app/").build();
    }

    @AllArgsConstructor
    @Getter
    @Setter
    private class Post
    {
        @JsonProperty(value = "manufacturer_code")
        private String manufacturerCode;
    }
    
    private String addProductApi(String manufacturerCode)
    {
        Post post = new Post(manufacturerCode);
        
        return webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
 
    public ResponseEntity<Integer> findProduct(String manufacturerCode)
    {
        Optional<Product> product = productRepository.findByManufacturerCode(manufacturerCode);

        if (product.isPresent())
            return ResponseEntity.ok(product.get().getId().intValue());
        else
            return ResponseEntity.ok(-1);
    }

    public ResponseEntity<Integer> addProduct(String manufacturerCode)
    {
        Optional<Product> product = productRepository.findByManufacturerCode(manufacturerCode);

        if (product.isEmpty())
        {
            //Product newProduct = new Product();
            //newProduct.setManufacturerCode(manufacturerCode);
            //productRepository.save(newProduct);

            Integer newProductId = Integer.parseInt(addProductApi(manufacturerCode));
            return ResponseEntity.ok(newProductId);
        }
        else
        {
            return ResponseEntity.ok(-1);
        }
    }

    public ResponseEntity<Product> getProduct(Long id)
    {
        Optional<Product> product = productRepository.findById(id);

        if (product.isPresent())
            return ResponseEntity.ok(product.get());
        else
            return ResponseEntity.ok(null);
    }

    public ResponseEntity<List<Product>> getLatestProducts()
    {
        List<Product> products = productRepository.findTop10ByOrderByCreatedAtDesc();
        return ResponseEntity.ok(products);
    }
}