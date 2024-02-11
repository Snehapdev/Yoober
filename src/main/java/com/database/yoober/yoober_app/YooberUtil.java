package com.database.yoober.yoober_app;

public class YooberUtil {
    public static boolean isValidCreditCardNumber(String creditCardNumber) {
        // Add your credit card number validation logic here
        // This can involve using regular expressions, Luhn algorithm, or external libraries
        // For simplicity, let's assume the credit card number must be a 16-digit number
        return creditCardNumber != null && creditCardNumber.matches("\\d{16}");
    }
}
