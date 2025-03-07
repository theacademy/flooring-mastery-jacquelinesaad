package com.js.flooringmastery.service;

import com.js.flooringmastery.dao.OrderDao;
import com.js.flooringmastery.dao.ProductDao;
import com.js.flooringmastery.dao.TaxDao;
import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.dto.Product;
import com.js.flooringmastery.dto.Tax;
import com.js.flooringmastery.exception.InvalidOrderDataException;
import com.js.flooringmastery.exception.NoSuchOrderException;
import com.js.flooringmastery.exception.PersistenceException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final TaxDao taxDao;

    public OrderServiceImpl(OrderDao orderDao, ProductDao productDao, TaxDao taxDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.taxDao = taxDao;
    }

    /// Fetching orders
    @Override
    public List<Order> getOrdersByDate(LocalDate orderDate) {
        return orderDao.getAllOrders(orderDate);
    }

    @Override
    public Order getOrder(LocalDate orderDate, int orderNumber) {
        Order order = orderDao.getOrder(orderDate, orderNumber);
        if (order == null) {
            throw new NoSuchOrderException("Order not found!");
        }
        return order;
    }

    @Override
    public List<Product> getAllProducts() {
        return productDao.getAllProducts();
    }

    /// Creating & Editing orders
    @Override
    public Order createOrder(Order order) {
        validateOrder(order);

        /// Retrieve tax details for the given state from the tax DAO
        Tax tax = taxDao.getTaxByState(order.getTax().getStateAbbreviation());
        if (tax == null) {
            throw new InvalidOrderDataException("Invalid state: " + order.getTax().getStateAbbreviation());
        }

        /// Retrieve product details from the product DAO
        Product product = productDao.getProductByType(order.getProduct().getProductType());
        if (product == null) {
            throw new InvalidOrderDataException("Invalid product type. Please choose a valid product.");
        }

        order.setTax(tax);
        order.setProduct(product);
        /// Calculate the costs (material, labor, tax, and total)
        calculateOrderValues(order);

        if (order.getMaterialCost() == null || order.getLaborCost() == null || order.getTotal() == null) {
            throw new IllegalStateException("Order cost values were not calculated properly.");
        }

        int nextOrderNumber = orderDao.getNextOrderNumber();
        order.setOrderNumber(nextOrderNumber);
        orderDao.addOrder(order);

        saveOrders(order.getOrderDate());

        return order;
    }

    @Override
    public Order editOrder(Order existingOrder, String customerName, String state, String productType, BigDecimal area) {
        boolean needsRecalculation = false;

        /// Validate customer name
        if (!customerName.isBlank() && customerName.matches("[a-zA-Z0-9., ]+")) {
            existingOrder.setCustomerName(customerName);
        }

        /// Validate state
        if (!state.isBlank()) {
            Tax tax = taxDao.getTaxByState(state);
            if (tax != null) {
                existingOrder.setTax(tax);
                needsRecalculation = true;
            } else {
                throw new InvalidOrderDataException("Invalid state: " + state + ". We cannot sell there.");
            }
        }

        /// Validate product
        if (!productType.isBlank()) {
            Product product = productDao.getProductByType(productType);
            if (product != null) {
                existingOrder.setProduct(product);
                needsRecalculation = true;
            } else {
                throw new InvalidOrderDataException("Invalid product: " + productType + ". Please select an available product.");
            }
        }

        /// Validate area
        if (area != null && area.compareTo(BigDecimal.valueOf(100)) >= 0) {
            existingOrder.setArea(area);
            needsRecalculation = true;
        }

        /// Recalculate if needed (state, product or area change) with boolean helper
        if (needsRecalculation) {
            calculateOrderValues(existingOrder);
        }

        saveOrders(existingOrder.getOrderDate());

        return existingOrder;
    }

    /// Deleting & Validating orders
    @Override
    public void removeOrder(LocalDate orderDate, int orderNumber) {
        Order order = getOrder(orderDate, orderNumber);
        orderDao.removeOrder(orderDate, orderNumber);
    }

    /// Helpers for validating user inputs
    public boolean isValidState(String state) {
        return taxDao.getTaxByState(state) != null;
    }

    public boolean isValidProduct(String productType) {
        return productDao.getProductByType(productType) != null;
    }

    /// Helper Method to Validate Order
    private void validateOrder(Order order) {
        /// Validate Order Date: Must be in the future
        if (order.getOrderDate().isBefore(LocalDate.now())) {
            throw new InvalidOrderDataException("Order date must be in the future.");
        }

        /// Validate Customer Name: Cannot be blank & must follow name rules
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty() ||
                !order.getCustomerName().matches("[a-zA-Z0-9., ]+")) {
            throw new InvalidOrderDataException("Invalid customer name. Use only letters, numbers, spaces, commas, and periods.");
        }

        /// Validate State: Must exist in the tax file
        Tax tax = taxDao.getTaxByState(order.getTax().getStateAbbreviation());
        if (tax == null) {
            throw new InvalidOrderDataException("Invalid state: " + order.getTax().getStateAbbreviation() + ". We cannot sell there.");
        }
        order.setTax(tax);

        /// Validate Product Type: Must match available products (case-insensitive)
        Product product = productDao.getProductByType(order.getProduct().getProductType());
        if (product == null) {
            throw new InvalidOrderDataException("Invalid product: " + order.getProduct().getProductType() + ". Please select an available product.");
        }
        order.setProduct(product);

        /// Validate Area: Must be at least 100 sq ft
        if (order.getArea() == null || order.getArea().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new InvalidOrderDataException("Invalid area. Minimum order size is 100 sq ft.");
        }
    }

    /// Helper Method for Calculating Material Cost, Labor Cost, Tax, and Total
    public void calculateOrderValues(Order order) {
        BigDecimal materialCost = order.getArea()
                .multiply(order.getProduct().getCostPerSquareFoot())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal laborCost = order.getArea()
                .multiply(order.getProduct().getLaborCostPerSquareFoot())
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxAmount = (materialCost.add(laborCost))
                .multiply(order.getTax().getTaxRate().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)) /// to not round tax rate
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = materialCost.add(laborCost).add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        order.setMaterialCost(materialCost);
        order.setLaborCost(laborCost);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);
    }

    ///  Manual save by controller
    @Override
    public void saveOrders(LocalDate orderDate) {
        try {
            List<Order> ordersToSave = orderDao.getAllOrders(orderDate);
            if (ordersToSave.isEmpty()) {
                System.out.println("No orders to save for " + orderDate);
                return;
            }
            orderDao.saveOrdersForDate(orderDate, ordersToSave); // Only save this date's orders
            System.out.println("Orders saved for: " + orderDate);
        } catch (Exception e) {
            System.out.println("Error saving orders: " + e.getMessage());
        }
    }

    ///  Stretch goal
    @Override
    public void exportAllData() {
        try {
            orderDao.exportAllData();
        } catch (PersistenceException e) {
            System.out.println("Error exporting orders: " + e.getMessage());
        }
    }

}