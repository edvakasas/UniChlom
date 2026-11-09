package lt.samata.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * What the mechanic enters for one estimate.
 *
 * @param carClass   name of a {@link CarClass}; kept as text so an unknown value becomes a validation error
 * @param priceLimit the most the customer agreed to pay, with VAT, or null when not given
 */
public record EstimateRequest(String carClass,
                              boolean regularCustomer,
                              List<String> workCodes,
                              List<PartRequest> parts,
                              BigDecimal priceLimit) {

    public EstimateRequest {
        workCodes = workCodes == null ? List.of() : List.copyOf(workCodes);
        parts = parts == null ? List.of() : List.copyOf(parts);
    }

    /** A part with its purchase price per unit (without VAT) and quantity (pieces or litres). */
    public record PartRequest(String name, BigDecimal unitCost, BigDecimal qty) {
    }
}
