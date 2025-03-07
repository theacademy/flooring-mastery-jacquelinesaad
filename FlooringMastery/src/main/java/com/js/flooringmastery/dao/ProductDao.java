package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Product;
import java.util.List;

public interface ProductDao {

    List<Product> getAllProducts();

    Product getProductByType(String productType);

}
