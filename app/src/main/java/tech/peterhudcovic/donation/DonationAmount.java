package tech.peterhudcovic.donation;

import java.math.BigDecimal;
import java.util.Set;

final class DonationAmount {
    private static final Set<String> PRESETS = Set.of("5", "10", "25");
    private static final BigDecimal MAXIMUM = new BigDecimal("10000.00");

    private DonationAmount() {
    }

    static BigDecimal validate(String preset, String custom) {
        String amount;
        if (custom != null && !custom.isBlank()) {
            amount = custom.strip();
        } else if (preset != null && PRESETS.contains(preset)) {
            amount = preset;
        } else {
            throw new IllegalArgumentException("Choose a preset or enter a custom amount.");
        }

        if (!amount.matches("[0-9]{1,5}(\\.[0-9]{1,2})?")) {
            throw new IllegalArgumentException("Enter a valid amount with up to two decimal places.");
        }
        BigDecimal value = new BigDecimal(amount);
        if (value.signum() <= 0 || value.compareTo(MAXIMUM) > 0) {
            throw new IllegalArgumentException("Enter an amount between EUR 0.01 and EUR 10,000.00.");
        }
        return value.setScale(2);
    }
}
