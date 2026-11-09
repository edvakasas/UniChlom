package lt.samata.domain.discount;

import lt.samata.domain.WorkItem;

import java.math.BigDecimal;
import java.util.Optional;

/** The promotion the workshop has set on a particular job, e.g. 20 % off diagnostics. */
public class PromotionDiscount implements DiscountRule {

    @Override
    public Optional<BigDecimal> percentFor(WorkItem work, DiscountContext context) {
        return Optional.ofNullable(work.promotionPercent());
    }
}
