package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDaoImplTest {
    private ProductDaoImpl productDao;

    @BeforeEach
    void setUp() {
        productDao = new ProductDaoImpl();
    }

    @Test
    void testGetAllProducts() {
        List<Product> products = productDao.getAllProducts();

        assertFalse(products.isEmpty(), "Product list should not be empty");

        assertTrue(products.stream().anyMatch(p -> p.getProductType().equalsIgnoreCase("Wood")),
                "Product list should contain 'Wood'");
    }

    @Test
    void testGetProductByType() {
        Product product = productDao.getProductByType("Wood");

        assertNotNull(product, "Product 'Wood' should exist");

        assertEquals("Wood", product.getProductType());
        assertEquals(new BigDecimal("5.15"), product.getCostPerSquareFoot());
        assertEquals(new BigDecimal("4.75"), product.getLaborCostPerSquareFoot());
    }

    @Test
    void testGetProductByInvalidType() {
        Product product = productDao.getProductByType("Steel");

        assertNull(product, "Non-existent product should return null");
    }
}
