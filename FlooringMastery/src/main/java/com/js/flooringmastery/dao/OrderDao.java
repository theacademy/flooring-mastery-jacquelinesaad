package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.exception.PersistenceException;

import java.time.LocalDate;
import java.util.List;

public interface OrderDao {

    void addOrder(Order order);

    List<Order> getAllOrders(LocalDate orderDate);

    Order getOrder(LocalDate orderDate, int orderNumber);

    void removeOrder(LocalDate orderDate, int orderNumber);

    void saveOrders() throws PersistenceException;

    void saveOrdersForDate(LocalDate orderDate, List<Order> orders) throws PersistenceException;

    int getNextOrderNumber();

    void exportAllData() throws PersistenceException;
}
