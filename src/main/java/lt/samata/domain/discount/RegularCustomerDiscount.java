package lt.samata.domain.discount;

import lt.samata.domain.WorkItem;

import java.math.BigDecimal;
import java.util.Optional;

/** The workshop's regular-customer discount, given on every labour line (never on parts). */
public class RegularCustomerDiscount implements DiscountRule {

    @Override
    public Optional<BigDecimal> percentFor(WorkItem work, DiscountContext context) {
        if (!context.request().regularCustomer()) {
            return Optional.empty();
        }
        return Optional.of(context.priceList().regularCustomerDiscountPercent());
    }
}
