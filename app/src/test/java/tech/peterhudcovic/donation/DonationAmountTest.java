package tech.peterhudcovic.donation;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DonationAmountTest {
    @Test
    void acceptsOnlyOfferedPresets() {
        for (String preset : new String[]{"5", "10", "25"}) {
            assertEquals(new BigDecimal(preset).setScale(2), DonationAmount.validate(preset, null));
        }
        assertThrows(IllegalArgumentException.class, () -> DonationAmount.validate("7", null));
        assertThrows(IllegalArgumentException.class, () -> DonationAmount.validate(null, null));
    }

    @Test
    void customAmountOverridesPreset() {
        assertEquals(new BigDecimal("15.25"), DonationAmount.validate("10", " 15.25 "));
        assertEquals(new BigDecimal("5.00"), DonationAmount.validate("5", " "));
    }

    @Test
    void acceptsBoundaryAmounts() {
        assertEquals(new BigDecimal("0.01"), DonationAmount.validate(null, "0.01"));
        assertEquals(new BigDecimal("10000.00"), DonationAmount.validate(null, "10000"));
    }

    @Test
    void rejectsInvalidCustomAmountsWithoutFallingBackToPreset() {
        for (String invalid : new String[]{"0", "-1", "10000.01", "100000", "1.001", "1e2", "NaN", "<script>", "1,25"}) {
            assertThrows(IllegalArgumentException.class, () -> DonationAmount.validate("10", invalid), invalid);
        }
    }
}
