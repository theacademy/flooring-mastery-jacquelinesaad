package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Tax;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaxDaoImplTest {
    private TaxDaoImpl taxDao;

    @BeforeEach
    void setUp() {
        taxDao = new TaxDaoImpl();
    }

    @Test
    void testGetAllTaxes() {
        List<Tax> taxes = taxDao.getAllTaxes();

        assertFalse(taxes.isEmpty(), "Tax list should not be empty");

        assertTrue(taxes.stream().anyMatch(t -> t.getStateAbbreviation().equalsIgnoreCase("TX")),
                "Tax list should contain 'TX' (Texas)");
    }

    @Test
    void testGetTaxByState() {
        Tax tax = taxDao.getTaxByState("TX");

        assertNotNull(tax, "Tax for 'TX' should exist");

        assertEquals("TX", tax.getStateAbbreviation());
        assertEquals("Texas", tax.getStateName());
        assertEquals(new BigDecimal("4.45"), tax.getTaxRate());
    }

    @Test
    void testGetTaxByInvalidState() {
        Tax tax = taxDao.getTaxByState("ZZ");

        assertNull(tax, "Non-existent state 'ZZ' should return null");
    }
}