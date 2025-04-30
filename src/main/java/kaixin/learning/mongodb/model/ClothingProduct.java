package kaixin.learning.mongodb.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClothingProduct extends Product {

    private String size;
    private String color;
    private String material;

    public ClothingProduct(String name, String description, Double price,
                           String size, String color, String material) {
        super(name, description, price, "CLOTHING");
        this.size = size;
        this.color = color;
        this.material = material;
    }
}