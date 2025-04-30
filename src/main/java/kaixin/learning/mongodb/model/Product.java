package kaixin.learning.mongodb.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "products")
@Getter
@Setter
@NoArgsConstructor
public abstract class Product {

    @Id
    private String id;
    private String name;
    private String description;
    private Double price;
    private String productType;

    public Product(String name, String description, Double price, String productType) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.productType = productType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}