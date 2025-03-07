package com.js.flooringmastery.service;

import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.dto.Product;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

public interface OrderService {

    List<Order> getOrdersByDate(LocalDate orderDate);

    Order getOrder(LocalDate orderDate, int orderNumber);

    Order createOrder(Order order);

    Order editOrder(Order existingOrder, String customerName, String state, String productType, BigDecimal area);

    void removeOrder(LocalDate orderDate, int orderNumber);

    void saveOrders(LocalDate orderDate);

    List<Product> getAllProducts();

    boolean isValidState(String state);

    boolean isValidProduct(String productType);

    void exportAllData();
}
