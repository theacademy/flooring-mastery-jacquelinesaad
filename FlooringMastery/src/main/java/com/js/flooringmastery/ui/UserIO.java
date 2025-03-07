package com.js.flooringmastery.ui;

public interface UserIO {

    void print(String message);

    String readString(String prompt);

    int readInt(String prompt, int min, int max);

    double readDouble(String prompt, double min, double max);

    java.math.BigDecimal readBigDecimal(String prompt);

    java.time.LocalDate readLocalDate(String prompt);
}
