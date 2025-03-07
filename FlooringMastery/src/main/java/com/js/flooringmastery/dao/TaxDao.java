package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Tax;
import java.util.List;

public interface TaxDao {

    List<Tax> getAllTaxes();

    /// Tax for specific state
    Tax getTaxByState(String stateAbbreviation);
}
