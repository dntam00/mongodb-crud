package kaixin.learning.mongodb;

import kaixin.learning.mongodb.model.ClothingProduct;
import kaixin.learning.mongodb.model.ElectronicProduct;
import kaixin.learning.mongodb.model.Product;
import kaixin.learning.mongodb.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testCreateAndLoadDifferentProductTypes() {
        // Clean up any existing data
        productRepository.deleteAll();

        // Create an electronic product
        ElectronicProduct laptop = new ElectronicProduct(
                "Laptop Pro",
                "High performance laptop for developers",
                1299.99,
                "TechBrand",
                "Developer X5",
                24
        );

        // Create a clothing product
        ClothingProduct tshirt = new ClothingProduct(
                "Casual T-Shirt",
                "Comfortable cotton t-shirt for daily wear",
                29.99,
                "L",
                "Black",
                "100% Cotton"
        );

        // Save both products
        ElectronicProduct savedLaptop = (ElectronicProduct) productRepository.save(laptop);
        ClothingProduct savedTshirt = (ClothingProduct) productRepository.save(tshirt);

        // Verify products were saved with IDs
        assertNotNull(savedLaptop.getId());
        assertNotNull(savedTshirt.getId());

        // Load electronic product and verify specific properties
        Optional<Product> retrievedLaptopOpt = productRepository.findById(savedLaptop.getId());
        assertTrue(retrievedLaptopOpt.isPresent());

        ElectronicProduct retrievedLaptop = (ElectronicProduct) retrievedLaptopOpt.get();
        assertEquals("ELECTRONIC", retrievedLaptop.getProductType());
        assertEquals("TechBrand", retrievedLaptop.getBrand());
        assertEquals("Developer X5", retrievedLaptop.getModel());
        assertEquals(24, retrievedLaptop.getWarrantyMonths());

        // Load clothing product and verify specific properties
        Optional<Product> retrievedTshirtOpt = productRepository.findById(savedTshirt.getId());
        assertTrue(retrievedTshirtOpt.isPresent());

        ClothingProduct retrievedTshirt = (ClothingProduct) retrievedTshirtOpt.get();
        assertEquals("CLOTHING", retrievedTshirt.getProductType());
        assertEquals("L", retrievedTshirt.getSize());
        assertEquals("Black", retrievedTshirt.getColor());
        assertEquals("100% Cotton", retrievedTshirt.getMaterial());

        // Test finding by product type
        List<Product> electronicProducts = productRepository.findByProductType("ELECTRONIC");
        assertEquals(1, electronicProducts.size());

        List<Product> clothingProducts = productRepository.findByProductType("CLOTHING");
        assertEquals(1, clothingProducts.size());

        // Verify total count
        assertEquals(2, productRepository.count());
    }
}