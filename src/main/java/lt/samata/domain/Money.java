package lt.samata.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** The one place where money is rounded (rule R3): to the cent, 0.005 rounds up. */
public final class Money {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private Money() {
    }

    public static BigDecimal round(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal percentOf(BigDecimal amount, BigDecimal percent) {
        return round(amount.multiply(percent).divide(HUNDRED));
    }
}
