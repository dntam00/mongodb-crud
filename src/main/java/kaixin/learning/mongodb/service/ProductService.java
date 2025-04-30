package kaixin.learning.mongodb.service;

import kaixin.learning.mongodb.model.Product;
import kaixin.learning.mongodb.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public List<Product> findByProductType(String productType) {
        return productRepository.findByProductType(productType);
    }

    // Other standard CRUD methods

    public List<Product> findByNameContaining(String name) {
        return productRepository.findByNameContaining(name);
    }

    public List<Product> findByPriceLessThan(Double price) {
        return productRepository.findByPriceLessThan(price);
    }

    public boolean existsById(String id) {
        return productRepository.existsById(id);
    }

    public void deleteById(String id) {
        productRepository.deleteById(id);
    }
}