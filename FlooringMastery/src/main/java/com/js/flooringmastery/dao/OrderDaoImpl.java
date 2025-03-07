package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.dto.Product;
import com.js.flooringmastery.dto.Tax;
import com.js.flooringmastery.exception.PersistenceException;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.time.format.DateTimeFormatter;

@Repository
public class OrderDaoImpl implements OrderDao {
    private static final String ORDERS_FOLDER = "SampleFileData/Orders/"; // Folder containing order files
    private static final String DELIMITER = ",";
    private final Map<LocalDate, List<Order>> ordersMap = new HashMap<>();

    public OrderDaoImpl(TaxDao taxDao, ProductDao productDao) {
        loadOrders();
    }

    @Override
    public void addOrder(Order order) {
        ordersMap.computeIfAbsent(order.getOrderDate(), k -> new ArrayList<>()).add(order);
    }

    @Override
    public List<Order> getAllOrders(LocalDate orderDate) {
        return ordersMap.getOrDefault(orderDate, new ArrayList<>());
    }

    @Override
    public Order getOrder(LocalDate orderDate, int orderNumber) {
        return ordersMap.getOrDefault(orderDate, new ArrayList<>())
                .stream()
                .filter(order -> order.getOrderNumber() == orderNumber)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void removeOrder(LocalDate orderDate, int orderNumber) {
        List<Order> orders = ordersMap.get(orderDate);
        if (orders != null) {
            orders.removeIf(order -> order.getOrderNumber() == orderNumber);
        }
    }

    @Override
    public void saveOrders() throws PersistenceException {
        for (Map.Entry<LocalDate, List<Order>> entry : ordersMap.entrySet()) {
            saveOrdersForDate(entry.getKey(), entry.getValue());
        }
    }

    /// For saving orders to a specific date
    public void saveOrdersForDate(LocalDate orderDate, List<Order> orders) throws PersistenceException {
        String folderPath = "SampleFileData/Orders";
        String fileName = String.format("%s/Orders_%s.txt", folderPath,
                orderDate.format(java.time.format.DateTimeFormatter.ofPattern("MMddyyyy")));

        File directory = new File(folderPath);
        if (!directory.exists()) {
            directory.mkdirs(); /// Creates the directory if it doesn't exist
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

            /// Write the header
            writer.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot," +
                    "LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");

            /// Write each order
            for (Order order : orders) {
                writer.println(marshalOrder(order));
            }
        } catch (IOException e) {
            System.out.println("Error saving orders: " + e.getMessage());
            throw new PersistenceException("Could not save order data.", e);
        }
    }

    /// For loading all orders files (scans orders folder)
    private void loadOrders() {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(ORDERS_FOLDER), "Orders_*.txt")) {
            for (Path file : files) {
                loadOrdersFromFile(file);
            }
        } catch (IOException e) {
            System.out.println("Error loading order files.");
        }
    }

    ///  For loading specific order file
    private void loadOrdersFromFile(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString();
        String dateString = fileName.substring(7, 15); /// Extracts MMDDYYYY
        LocalDate orderDate = LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern("MMddyyyy"));

        try (Scanner scanner = new Scanner(filePath)) {
            scanner.nextLine(); /// Skip header
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Order order = unmarshalOrder(line, orderDate);
                addOrder(order);
            }
        }
    }

    /// Helper for keeping track of order numbers
    /// Reads all existing orders to find the highest order number, then increments by 1
    public int getNextOrderNumber() {
        int highestOrderNumber = 0;

        try (DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(ORDERS_FOLDER), "Orders_*.txt")) {
            for (Path file : files) {
                try (Scanner scanner = new Scanner(file)) {
                    scanner.nextLine(); /// Skip header

                    while (scanner.hasNextLine()) {
                        String[] tokens = scanner.nextLine().split(DELIMITER);
                        int orderNumber = Integer.parseInt(tokens[0]);

                        if (orderNumber > highestOrderNumber) {
                            highestOrderNumber = orderNumber;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading order files: " + e.getMessage());
        }

        return highestOrderNumber + 1; /// Assign next available order number
    }

    ///  Converts an Order object into a format string for saving
    private Order unmarshalOrder(String line, LocalDate orderDate) {
        String[] tokens = line.split(DELIMITER);

        /// Extract order data
        int orderNumber = Integer.parseInt(tokens[0]);
        String customerName = tokens[1];

        /// Extract tax info
        String stateAbbreviation = tokens[2];
        BigDecimal taxRate = new BigDecimal(tokens[3]);
        Tax tax = new Tax(stateAbbreviation, "", taxRate); // Tax name is unused in orders file

        /// Extract product info
        String productType = tokens[4];
        BigDecimal costPerSquareFoot = new BigDecimal(tokens[6]);
        BigDecimal laborCostPerSquareFoot = new BigDecimal(tokens[7]);
        Product product = new Product(productType, costPerSquareFoot, laborCostPerSquareFoot);

        /// Extract order values
        BigDecimal area = new BigDecimal(tokens[5]);
        BigDecimal materialCost = new BigDecimal(tokens[8]);
        BigDecimal laborCost = new BigDecimal(tokens[9]);
        BigDecimal taxAmount = new BigDecimal(tokens[10]);
        BigDecimal total = new BigDecimal(tokens[11]);

        /// Create order object
        Order order = new Order(orderNumber, customerName, tax, product, area, orderDate);

        /// Set costs
        order.setMaterialCost(materialCost);
        order.setLaborCost(laborCost);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);

        return order;
    }

    /// Reads an order line from a file and converts it into an Order object
    private String marshalOrder(Order order) {
        return String.join(DELIMITER,
                String.valueOf(order.getOrderNumber()),
                order.getCustomerName(),
                order.getTax().getStateAbbreviation(),
                order.getTax().getTaxRate().toString(),
                order.getProduct().getProductType(),
                order.getArea().toString(),
                order.getProduct().getCostPerSquareFoot().toString(),
                order.getProduct().getLaborCostPerSquareFoot().toString(),
                order.getMaterialCost().toString(),
                order.getLaborCost().toString(),
                order.getTaxAmount().toString(),
                order.getTotal().toString()
        );
    }

    /// Stretch goal - Export data
    public void exportAllData() throws PersistenceException {
        String backupFolderPath = "SampleFileData/Backup";
        String exportFilePath = backupFolderPath + "/DataExport.txt";

        File directory = new File(backupFolderPath);
        if (!directory.exists()) {
            directory.mkdirs();  /// Ensure the Backup folder exists
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(exportFilePath))) {
            /// Write header
            writer.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,"
                    + "LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total,OrderDate");

            /// Collect all orders from all files and sort by Order Number
            List<Order> allOrders = ordersMap.values().stream()
                    .flatMap(List::stream)
                    .sorted(Comparator.comparingInt(Order::getOrderNumber))
                    .toList();

            for (Order order : allOrders) {
                writer.println(marshalOrderForExport(order, order.getOrderDate()));
            }

        } catch (IOException e) {
            throw new PersistenceException("Could not export data.", e);
        }
    }

    /// Helper method for exportAllData() like marshalOrder but with order date added
    private String marshalOrderForExport(Order order, LocalDate orderDate) {
        return String.join(",",
                String.valueOf(order.getOrderNumber()),
                order.getCustomerName(),
                order.getTax().getStateAbbreviation(),
                order.getTax().getTaxRate().toString(),
                order.getProduct().getProductType(),
                order.getArea().toString(),
                order.getProduct().getCostPerSquareFoot().toString(),
                order.getProduct().getLaborCostPerSquareFoot().toString(),
                order.getMaterialCost().toString(),
                order.getLaborCost().toString(),
                order.getTaxAmount().toString(),
                order.getTotal().toString(),
                orderDate.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")) // Format the date
        );
    }

}
