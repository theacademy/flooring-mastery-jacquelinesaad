package com.js.flooringmastery.service;

import com.js.flooringmastery.dao.OrderDao;
import com.js.flooringmastery.dao.ProductDao;
import com.js.flooringmastery.dao.TaxDao;
import com.js.flooringmastery.dto.Order;
import com.js.flooringmastery.dto.Product;
import com.js.flooringmastery.dto.Tax;
import com.js.flooringmastery.exception.InvalidOrderDataException;
import com.js.flooringmastery.exception.NoSuchOrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {
    private OrderServiceImpl orderService;
    private OrderDao orderDao;
    private ProductDao productDao;
    private TaxDao taxDao;


    ///  Using Mockito to create mock objects for testing
    @BeforeEach
    void setUp() {
        orderDao = Mockito.mock(OrderDao.class);
        productDao = Mockito.mock(ProductDao.class);
        taxDao = Mockito.mock(TaxDao.class);
        orderService = new OrderServiceImpl(orderDao, productDao, taxDao);
    }

    @Test
    void testCreateOrder_Success() {
        LocalDate orderDate = LocalDate.of(2025, 6, 18);

        Product mockProduct = new Product("Wood", new BigDecimal("5.15"), new BigDecimal("4.75"));
        Tax mockTax = new Tax("TX", "Texas", new BigDecimal("4.45"));

        Order order = new Order(4, "Jackie", mockTax, mockProduct, new BigDecimal("200"), orderDate);

        when(taxDao.getTaxByState("TX")).thenReturn(mockTax);
        when(productDao.getProductByType("Wood")).thenReturn(mockProduct);

        /// Mock getAllProducts() to return "Wood"
        when(productDao.getAllProducts()).thenReturn(List.of(mockProduct));

        when(orderDao.getNextOrderNumber()).thenReturn(10);
        Order createdOrder = orderService.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals(10, createdOrder.getOrderNumber());
        verify(orderDao, times(1)).addOrder(createdOrder);
    }

    @Test
    void testCreateOrder_InvalidState() {
        Order order = new Order(1, "Jackie", new Tax("ZZ", "InvalidState", new BigDecimal("0")),
                new Product("Wood", new BigDecimal("5.15"), new BigDecimal("4.75")),
                new BigDecimal("200"), LocalDate.of(2025, 6, 18));

        when(taxDao.getTaxByState("ZZ")).thenReturn(null);

        assertThrows(InvalidOrderDataException.class, () -> orderService.createOrder(order));
    }

    @Test
    void testCreateOrder_InvalidProduct() {
        Order order = new Order(1, "Jackie", new Tax("TX", "Texas", new BigDecimal("6.25")),
                new Product("FakeProduct", BigDecimal.ZERO, BigDecimal.ZERO),
                new BigDecimal("200"), LocalDate.of(2025, 6, 18));

        when(taxDao.getTaxByState("TX")).thenReturn(order.getTax());
        when(productDao.getProductByType("FakeProduct")).thenReturn(null);

        assertThrows(InvalidOrderDataException.class, () -> orderService.createOrder(order));
    }

    @Test
    void testGetOrder_Valid() {
        LocalDate date = LocalDate.of(2025, 6, 19);
        Order order = new Order(2, "Alice", new Tax("CA", "California", new BigDecimal("25.00")),
                new Product("Tile", new BigDecimal("3.50"), new BigDecimal("4.15")),
                new BigDecimal("150"), date);

        when(orderDao.getOrder(date, 2)).thenReturn(order);

        Order retrievedOrder = orderService.getOrder(date, 2);
        assertNotNull(retrievedOrder);
        assertEquals("Alice", retrievedOrder.getCustomerName());
    }

    @Test
    void testGetOrder_NotFound() {
        LocalDate date = LocalDate.of(2025, 6, 20);

        when(orderDao.getOrder(date, 3)).thenReturn(null);

        assertThrows(NoSuchOrderException.class, () -> orderService.getOrder(date, 3));
    }

    @Test
    void testEditOrder_Success() {
        LocalDate date = LocalDate.of(2025, 6, 21);
        Order existingOrder = new Order(3, "OriginalName", new Tax("KY", "Kentucky", new BigDecimal("6.00")),
                new Product("Carpet", new BigDecimal("2.25"), new BigDecimal("2.10")),
                new BigDecimal("120"), date);

        when(orderDao.getOrder(date, 3)).thenReturn(existingOrder);
        when(taxDao.getTaxByState("KY")).thenReturn(new Tax("KY", "Kentucky", new BigDecimal("6.00")));
        when(productDao.getProductByType("Tile")).thenReturn(new Product("Tile", new BigDecimal("3.50"), new BigDecimal("4.15")));

        Order editedOrder = orderService.editOrder(existingOrder, "NewName", "KY", "Tile", new BigDecimal("140"));

        assertEquals("NewName", editedOrder.getCustomerName());
        assertEquals("Tile", editedOrder.getProduct().getProductType());
        assertEquals(new BigDecimal("140"), editedOrder.getArea());
    }

    @Test
    void testRemoveOrder() {
        LocalDate date = LocalDate.of(2025, 6, 22);
        Order order = new Order(4, "Ronnie", new Tax("WA", "Washington", new BigDecimal("9.25")),
                new Product("Laminate", new BigDecimal("1.75"), new BigDecimal("2.10")),
                new BigDecimal("200"), date);

        when(orderDao.getOrder(date, 4)).thenReturn(order);

        orderService.removeOrder(date, 4);

        verify(orderDao, times(1)).removeOrder(date, 4);
    }

    @Test
    void testIsValidState_Valid() {
        when(taxDao.getTaxByState("TX")).thenReturn(new Tax("TX", "Texas", new BigDecimal("4.45")));

        assertTrue(orderService.isValidState("TX"));
    }

    @Test
    void testIsValidState_Invalid() {
        when(taxDao.getTaxByState("ZZ")).thenReturn(null);

        assertFalse(orderService.isValidState("ZZ"));
    }

    @Test
    void testIsValidProduct_Valid() {
        when(productDao.getProductByType("Wood")).thenReturn(new Product("Wood", new BigDecimal("5.15"), new BigDecimal("4.75")));

        assertTrue(orderService.isValidProduct("Wood"));
    }

    @Test
    void testIsValidProduct_Invalid() {
        when(productDao.getProductByType("FakeProduct")).thenReturn(null);

        assertFalse(orderService.isValidProduct("FakeProduct"));
    }

    @Test
    void testCalculateOrderValues() {
        Order order = new Order(1, "Ada Lovelace",
                new Tax("CA", "California", new BigDecimal("25.00")),
                new Product("Tile", new BigDecimal("3.50"), new BigDecimal("4.15")),
                new BigDecimal("249.00"),
                LocalDate.of(2025, 7, 1));

        orderService.calculateOrderValues(order);

        assertNotNull(order.getMaterialCost());
        assertNotNull(order.getLaborCost());
        assertNotNull(order.getTaxAmount());
        assertNotNull(order.getTotal());

        assertEquals(new BigDecimal("871.50"), order.getMaterialCost());  // 249 * 3.50
        assertEquals(new BigDecimal("1033.35"), order.getLaborCost());    // 249 * 4.15
        assertEquals(new BigDecimal("476.21"), order.getTaxAmount());     // (871.50 + 1033.35) * 25 / 100
        assertEquals(new BigDecimal("2381.06"), order.getTotal());        // material + labor + tax
    }

}
