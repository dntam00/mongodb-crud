package kaixin.learning.mongodb.repository;

import kaixin.learning.mongodb.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByNameContaining(String name);

    List<Product> findByPriceLessThan(Double price);

    List<Product> findByProductType(String productType);
}