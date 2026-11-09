package lt.samata.domain.discount;

import lt.samata.domain.EstimateRequest;
import lt.samata.domain.PriceList;

import java.math.BigDecimal;

/**
 * What a discount rule may look at besides the job itself.
 *
 * @param totalBilledHours billed hours of all labour lines together (after rules R1 and R2)
 */
public record DiscountContext(EstimateRequest request, PriceList priceList, BigDecimal totalBilledHours) {
}
