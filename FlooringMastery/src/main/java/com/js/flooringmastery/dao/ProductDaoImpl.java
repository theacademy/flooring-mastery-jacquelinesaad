package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Product;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class ProductDaoImpl implements ProductDao {
    private static final String PRODUCT_FILE = "SampleFileData/Data/Products.txt";
    private static final String DELIMITER = ",";
    private final Map<String, Product> products = new HashMap<>();

    public ProductDaoImpl() {
        loadProductData();
    }

    @Override
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    @Override
    public Product getProductByType(String productType) {
        return products.get(productType);
    }

    private void loadProductData() {
        try (Scanner scanner = new Scanner(Files.newBufferedReader(Paths.get(PRODUCT_FILE)))) {
            scanner.nextLine(); /// Skip header row

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Product product = unmarshalProduct(line);
                products.put(product.getProductType(), product);
            }
        } catch (IOException e) {
            System.out.println("Error loading product data.");
        }
    }

    private Product unmarshalProduct(String line) {
        String[] tokens = line.split(DELIMITER);
        return new Product(
                tokens[0], /// ProductType
                new BigDecimal(tokens[1]), /// CostPerSquareFoot
                new BigDecimal(tokens[2])  /// LaborCostPerSquareFoot
        );
    }
}
