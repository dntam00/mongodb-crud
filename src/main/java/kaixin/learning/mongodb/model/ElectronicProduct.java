package kaixin.learning.mongodb.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ElectronicProduct extends Product {

    private String brand;
    private String model;
    private Integer warrantyMonths;

    public ElectronicProduct(String name, String description, Double price,
                             String brand, String model, Integer warrantyMonths) {
        super(name, description, price, "ELECTRONIC");
        this.brand = brand;
        this.model = model;
        this.warrantyMonths = warrantyMonths;
    }
}