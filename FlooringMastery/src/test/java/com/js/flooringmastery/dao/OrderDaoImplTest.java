package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.dto.Product;
import com.js.flooringmastery.dto.Tax;
import com.js.flooringmastery.exception.PersistenceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import com.js.flooringmastery.service.OrderServiceImpl;

class OrderDaoImplTest {
    private OrderDaoImpl orderDao;
    private TaxDao taxDao;
    private ProductDao productDao;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() throws Exception {
        taxDao = new TaxDaoImpl();
        productDao = new ProductDaoImpl();
        orderDao = new OrderDaoImpl(taxDao, productDao);
        orderService = new OrderServiceImpl(orderDao, productDao, taxDao);
    }

    @Test
    void testAddAndGetOrder() {
        LocalDate orderDate = LocalDate.of(2025, 6, 15);

        Tax tax = taxDao.getTaxByState("TX");
        Product product = productDao.getProductByType("Wood");

        Order order = new Order(4, "Jackie Saad", tax, product, new BigDecimal("250"), orderDate);
        orderDao.addOrder(order);

        Order retrievedOrder = orderDao.getOrder(orderDate, 4);
        assertNotNull(retrievedOrder);
        assertEquals("Jackie Saad", retrievedOrder.getCustomerName());
        assertEquals("TX", retrievedOrder.getTax().getStateAbbreviation());
        assertEquals("Wood", retrievedOrder.getProduct().getProductType());
    }

    @Test
    void testGetAllOrders() {
        LocalDate orderDate = LocalDate.of(2025, 6, 16);

        Tax taxCA = taxDao.getTaxByState("CA");
        Product productCarpet = productDao.getProductByType("Carpet");

        Tax taxTX = taxDao.getTaxByState("TX");
        Product productTile = productDao.getProductByType("Tile");

        /// Create orders
        Order order1 = new Order(5, "Billy", taxCA, productCarpet, new BigDecimal("400"), orderDate);
        Order order2 = new Order(6, "Bob", taxTX, productTile, new BigDecimal("550"), orderDate);

        orderDao.addOrder(order1);
        orderDao.addOrder(order2);

        /// Verify orders are retrieved correctly
        List<Order> orders = orderDao.getAllOrders(orderDate);
        assertEquals(2, orders.size());
        assertEquals("Billy", orders.get(0).getCustomerName());
        assertEquals("Bob", orders.get(1).getCustomerName());
    }

    @Test
    void testRemoveOrder() {
        LocalDate orderDate = LocalDate.of(2025, 6, 17);

        Tax taxWA = taxDao.getTaxByState("WA");
        Product productLaminate = productDao.getProductByType("Laminate");

        Order order = new Order(7, "Mr. WillBeRemoved", taxWA, productLaminate, new BigDecimal("120"), orderDate);
        orderDao.addOrder(order);

        orderDao.removeOrder(orderDate, 7);
        assertNull(orderDao.getOrder(orderDate, 7));
    }

    @Test
    void testSaveAndLoadOrders() throws PersistenceException {
        LocalDate orderDate = LocalDate.of(2025, 6, 18);

        Tax taxKY = taxDao.getTaxByState("KY");
        Product productWood = productDao.getProductByType("Wood");

        Order order = new Order(4, "Ronnie", taxKY, productWood, new BigDecimal("800"), orderDate);

        orderService.calculateOrderValues(order); ///Loaded from service layer
        orderDao.addOrder(order);
        orderDao.saveOrders();

        OrderDaoImpl newDao = new OrderDaoImpl(new TaxDaoImpl(), new ProductDaoImpl());
        List<Order> loadedOrders = newDao.getAllOrders(orderDate);

        assertFalse(loadedOrders.isEmpty());
        assertEquals(1, loadedOrders.size());
        assertEquals("Ronnie", loadedOrders.get(0).getCustomerName());
    }

    /// Removes test file, comment out to view generated text file
    @AfterEach
    void cleanupTestFile() {
        new File("SampleFileData/Orders/Orders_06182025.txt").delete();
    }

    /// Stretch goal test
    @Test
    void testExportAllData() throws PersistenceException, IOException {
        /// Create mock orders and save them
        LocalDate orderDate = LocalDate.of(2025, 6, 18);
        Tax tax = taxDao.getTaxByState("TX");
        Product product = productDao.getProductByType("Wood");

        Order order1 = new Order(4, "Mrs. TestingExporting", tax, product, new BigDecimal("250"), orderDate);
        orderService.calculateOrderValues(order1);
        orderDao.addOrder(order1);
        orderDao.saveOrders();
        orderDao.exportAllData();

        Path exportFilePath = Paths.get("SampleFileData/Backup/DataExport.txt");
        assertTrue(Files.exists(exportFilePath), "Export file should exist!");

        /// Read the exported file and check contents
        List<String> lines = Files.readAllLines(exportFilePath);
        assertFalse(lines.isEmpty(), "Export file should not be empty!");

        /// Check the header
        String expectedHeader = "OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,"
                + "LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total,OrderDate";
        assertEquals(expectedHeader, lines.get(0), "Header should match expected format!");

        /// Check the first order entry
        String[] orderFields = lines.get(1).split(",");
        assertEquals(13, orderFields.length, "Each order should have 13 fields including the date!");

        /// Check the date format
        assertTrue(orderFields[12].matches("\\d{2}-\\d{2}-\\d{4}"), "Order date should be in MM-DD-YYYY format!");
    }
}
