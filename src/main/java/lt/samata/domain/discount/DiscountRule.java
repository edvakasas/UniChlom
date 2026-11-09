package lt.samata.domain.discount;

import lt.samata.domain.WorkItem;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * One kind of discount for a labour line (strategy pattern).
 * The calculator asks every rule and applies only the largest percentage (rule R4),
 * so a new discount is a new class, not a change to the calculator.
 */
public interface DiscountRule {

    /** @return the discount percentage this rule gives the job, or empty when the rule does not apply */
    Optional<BigDecimal> percentFor(WorkItem work, DiscountContext context);
}
