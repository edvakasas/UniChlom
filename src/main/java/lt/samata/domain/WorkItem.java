package lt.samata.domain;

import java.math.BigDecimal;

/**
 * A job from a workshop's price list.
 *
 * @param sharedOperation      operation this job shares with other jobs (e.g. removing the front wheels), or null
 * @param sharedOperationHours hours of that shared operation included in {@code normHours}
 * @param promotionPercent     promotion discount for this job, or null when there is none
 */
public record WorkItem(String code,
                       String name,
                       BigDecimal normHours,
                       String sharedOperation,
                       BigDecimal sharedOperationHours,
                       BigDecimal promotionPercent) {

    public boolean hasSharedOperation() {
        return sharedOperation != null;
    }
}
