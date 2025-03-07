package com.js.flooringmastery.dao;

import com.js.flooringmastery.dto.Tax;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Repository
public class TaxDaoImpl implements TaxDao {
    private static final String TAX_FILE = "SampleFileData/Data/Taxes.txt";
    private static final String DELIMITER = ",";
    private final Map<String, Tax> taxes = new HashMap<>();

    public TaxDaoImpl() {
        loadTaxData();
    }

    @Override
    public List<Tax> getAllTaxes() {
        return new ArrayList<>(taxes.values());
    }

    @Override
    public Tax getTaxByState(String stateAbbreviation) {
        return taxes.get(stateAbbreviation);
    }

    private void loadTaxData() {
        try (Scanner scanner = new Scanner(Files.newBufferedReader(Paths.get(TAX_FILE)))) {
            scanner.nextLine(); /// Skip header row

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Tax tax = unmarshalTax(line);
                taxes.put(tax.getStateAbbreviation(), tax);
            }
        } catch (IOException e) {
            System.out.println("Error loading tax data.");
        }
    }

    /// Converts strings from txt file into Tax object
    private Tax unmarshalTax(String line) {
        String[] tokens = line.split(DELIMITER);
        return new Tax(
                tokens[0], /// StateAbbreviation
                tokens[1], /// StateName
                new BigDecimal(tokens[2]) /// TaxRate
        );
    }
}
