package com.js.flooringmastery.controller;

import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.dto.Product;
import com.js.flooringmastery.dto.Tax;
import com.js.flooringmastery.exception.InvalidOrderDataException;
import com.js.flooringmastery.service.OrderService;
import com.js.flooringmastery.ui.OrderView;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class OrderController {
    private final OrderService service;
    private final OrderView view;

    public OrderController(OrderService service, OrderView view) {
        this.service = service;
        this.view = view;
    }

    public void run() {
        boolean keepRunning = true;
        while (keepRunning) {
            int choice = view.displayMenu();
            switch (choice) {
                case 1 -> displayOrders();
                case 2 -> addOrder();
                case 3 -> editOrder();
                case 4 -> removeOrder();
                case 5 -> exportData();
                case 6 -> keepRunning = false;
            }
        }
        view.displayMessage("Exiting program...");
    }

    private void displayOrders() {
        LocalDate date = view.getOrderDate();
        List<Order> orders = service.getOrdersByDate(date);
        view.displayOrders(orders);
    }

    private void addOrder() {
        LocalDate orderDate;
        String customerName;
        String state;
        String productType;
        BigDecimal area;

        /// Ask for order date and validate
        while (true) {
            orderDate = view.getOrderDate();
            if (!orderDate.isBefore(LocalDate.now())) break;
            view.displayMessage("Error: Order date must be in the future. Try again.");
        }

        /// Ask for customer name and validate
        while (true) {
            customerName = view.getCustomerName();
            if (!customerName.isBlank() && customerName.matches("[a-zA-Z0-9., ]+")) break;
            view.displayMessage("Error: Customer name must contain only letters, numbers, spaces, commas, and periods. Try again.");
        }

        /// Ask for state and validate
        while (true) {
            state = view.getState();
            if (service.isValidState(state)) break;
            view.displayMessage("Error: Invalid state. We cannot sell there. Try again.");
        }

        /// Show products before asking for input
        List<Product> availableProducts = service.getAllProducts();
        view.displayAvailableProducts(availableProducts);

        /// Ask for product type and validate
        while (true) {
            productType = view.getProductType();
            if (service.isValidProduct(productType)) break;
            view.displayMessage("Error: Invalid product type. Please select an available product.");
        }

        /// Ask for area and validate
        while (true) {
            area = view.getArea();
            if (area.compareTo(BigDecimal.valueOf(100)) >= 0) break;
            view.displayMessage("Error: Minimum order size is 100 sq ft. Try again.");
        }

        /// Create order
        Order newOrder = new Order(0, customerName, new Tax(state, "", BigDecimal.ZERO),
                new Product(productType, BigDecimal.ZERO, BigDecimal.ZERO), area, orderDate);

        newOrder = service.createOrder(newOrder);
        view.displayOrderSummary(newOrder);
    }

    private void editOrder() {
        LocalDate date = view.getOrderDate();
        int orderNumber = view.getOrderNumber();
        Order existingOrder = service.getOrder(date, orderNumber);

        view.displayMessage("Press Enter to keep the existing value.");

        String customerName;
        while (true) {
            customerName = view.getCustomerName();
            if (customerName.isBlank() || customerName.matches("[a-zA-Z0-9., ]+")) break;
            view.displayMessage("Error: Customer name must contain only letters, numbers, spaces, commas, and periods. Try again.");
        }

        String state;
        while (true) {
            state = view.getState();
            if (state.isBlank() || service.isValidState(state)) break;
            view.displayMessage("Error: Invalid state. We cannot sell there. Try again.");
        }

        List<Product> availableProducts = service.getAllProducts();
        view.displayAvailableProducts(availableProducts);

        String productType;
        while (true) {
            productType = view.getProductType();
            if (productType.isBlank() || service.isValidProduct(productType)) break;
            view.displayMessage("Error: Invalid product type. Please select an available product.");
        }

        BigDecimal area;
        while (true) {
            area = view.getRawArea();
            if (area == null || area.compareTo(BigDecimal.valueOf(100)) >= 0) break;
            view.displayMessage("Error: Minimum order size is 100 sq ft. Try again.");
        }

        Order updatedOrder = service.editOrder(existingOrder, customerName, state, productType, area);
        view.displayOrder(updatedOrder);
        view.displayMessage("Order updated successfully.");
    }

    private void removeOrder() {
        LocalDate date = view.getOrderDate();
        int orderNumber = view.getOrderNumber();
        service.removeOrder(date, orderNumber);
        view.displayMessage("Order removed successfully.");
    }

    ///  Stretch goal - implemented
    private void exportData() {
        service.exportAllData();
        view.displayMessage("All orders exported to Backup/DataExport.txt");
    }
}
