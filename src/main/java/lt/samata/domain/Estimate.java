package lt.samata.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * A calculated estimate.
 *
 * @param overLimitBy how much the total exceeds the customer's price limit, or null when it does not
 */
public record Estimate(List<LaborLine> laborLines,
                       List<PartLine> partLines,
                       BigDecimal laborTotal,
                       BigDecimal partsTotal,
                       BigDecimal subtotal,
                       BigDecimal vat,
                       BigDecimal total,
                       EstimateStatus status,
                       BigDecimal overLimitBy) {

    public Estimate withStatus(EstimateStatus newStatus) {
        return new Estimate(laborLines, partLines, laborTotal, partsTotal, subtotal, vat, total, newStatus, overLimitBy);
    }

    /** @param discountPercent the one discount applied (0 when none); never a sum of several discounts */
    public record LaborLine(String code,
                            String name,
                            BigDecimal billedHours,
                            BigDecimal price,
                            BigDecimal discountPercent,
                            BigDecimal discountAmount,
                            BigDecimal finalPrice) {
    }

    public record PartLine(String name, BigDecimal unitCost, BigDecimal quantity, BigDecimal price) {
    }
}
