package com.js.flooringmastery.ui;

import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.dto.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class OrderView {
    private final UserIO io;

    public OrderView(UserIO io) {
        this.io = io;
    }

    public int displayMenu() {
        io.print("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
        io.print("* <<Flooring Program>>");
        io.print("* 1. Display Orders");
        io.print("* 2. Add an Order");
        io.print("* 3. Edit an Order");
        io.print("* 4. Remove an Order");
        io.print("* 5. Export All Data");
        io.print("* 6. Quit");
        io.print("*");
        io.print("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
        return io.readInt("Select an option: ", 1, 6);
    }

    public LocalDate getOrderDate() {
        return io.readLocalDate("Enter order date (YYYY-MM-DD): ");
    }

    public int getOrderNumber() {
        return io.readInt("Enter order number: ", 1, Integer.MAX_VALUE);
    }

    public String getCustomerName() {
        return io.readString("Enter customer name: ");
    }

    public String getState() {
        return io.readString("Enter state abbreviation (e.g., TX): ");
    }

    public String getProductType() {
        return io.readString("Enter product type: ");
    }

    public BigDecimal getArea() {
        return io.readBigDecimal("Enter area (minimum 100 sq ft): ");
    }

    public BigDecimal getRawArea() {
        String input = io.readString("Enter area (minimum 100 sq ft) or press Enter to keep existing area:");
        return input.isBlank() ? null : new BigDecimal(input);
    }

    public void displayOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            io.print("No orders found.");
            return;
        }
        for (Order order : orders) {
            displayOrderSummary(order);
        }
    }

    public void displayOrder(Order order) {
        displayOrderSummary(order);
    }

    public void displayMessage(String message) {
        io.print(message);
    }

    public void displayAvailableProducts(List<Product> products) {
        io.print("\nAvailable Products:");
        io.print("-------------------------------------------------");
        for (Product product : products) {
            io.print(String.format(" %-10s | Cost/SqFt: $%-6.2f | Labor Cost/SqFt: $%-6.2f",
                    product.getProductType(),
                    product.getCostPerSquareFoot(),
                    product.getLaborCostPerSquareFoot()));
        }
        io.print("-------------------------------------------------");
    }

    /// Outputs summary of order after adding the order
    public void displayOrderSummary(Order order) {
        io.print(String.format(" Order Number:    %d", order.getOrderNumber()));
        io.print(String.format(" Customer Name:   %s", order.getCustomerName()));
        io.print(String.format(" State:           %s", order.getTax().getStateAbbreviation()));
        io.print(String.format(" Tax Rate:        %.2f%%", order.getTax().getTaxRate()));
        io.print(String.format(" Product:         %s", order.getProduct().getProductType()));
        io.print(String.format(" Area:            %.2f SqFt", order.getArea()));
        io.print(String.format(" Material Cost:   $%.2f", order.getMaterialCost()));
        io.print(String.format(" Labor Cost:      $%.2f", order.getLaborCost()));
        io.print(String.format(" Tax Amount:      $%.2f", order.getTaxAmount()));
        io.print(String.format(" Total Cost:      $%.2f", order.getTotal()));
        io.print(String.format(" Order Date:      %s", order.getOrderDate()));
    }
}
