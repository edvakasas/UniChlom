package lt.samata.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/** One workshop's prices and rates. All amounts are without VAT; percentages are whole numbers (20 = 20 %). */
public record PriceList(BigDecimal hourlyRate,
                        BigDecimal partsMarkupPercent,
                        BigDecimal regularCustomerDiscountPercent,
                        BigDecimal vatPercent,
                        Map<CarClass, BigDecimal> carClassCoefficients,
                        Map<String, WorkItem> works) {

    public Optional<WorkItem> findWork(String code) {
        return Optional.ofNullable(works.get(code));
    }

    public BigDecimal coefficientFor(CarClass carClass) {
        return carClassCoefficients.get(carClass);
    }
}
