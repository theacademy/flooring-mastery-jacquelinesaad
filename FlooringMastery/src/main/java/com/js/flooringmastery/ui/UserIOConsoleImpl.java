package com.js.flooringmastery.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class UserIOConsoleImpl implements UserIO {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void print(String message) {
        System.out.println(message);
    }

    @Override
    public String readString(String prompt) {
        print(prompt);
        return scanner.nextLine().trim();
    }

    @Override
    public int readInt(String prompt, int min, int max) {
        while (true) {
            try {
                int value = Integer.parseInt(readString(prompt));
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException e) {
                print("Invalid input. Enter a number between " + min + " and " + max + ".");
            }
        }
    }

    @Override
    public double readDouble(String prompt, double min, double max) {
        while (true) {
            try {
                double value = Double.parseDouble(readString(prompt));
                if (value >= min && value <= max) return value;
            } catch (NumberFormatException e) {
                print("Invalid input. Enter a decimal between " + min + " and " + max + ".");
            }
        }
    }

    @Override
    public BigDecimal readBigDecimal(String prompt) {
        while (true) {
            try {
                return new BigDecimal(readString(prompt));
            } catch (NumberFormatException e) {
                print("Invalid input. Enter a valid decimal number.");
            }
        }
    }

    @Override
    public LocalDate readLocalDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(readString(prompt));
            } catch (DateTimeParseException e) {
                print("Invalid date format. Please enter as YYYY-MM-DD.");
            }
        }
    }
}
