package lt.samata.domain;

import lt.samata.domain.EstimateRequest.PartRequest;
import lt.samata.domain.discount.PromotionDiscount;
import lt.samata.domain.discount.RegularCustomerDiscount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Scenarios from section 3 of the Stage I design document, with "Autoservisas Alfa" prices. */
class EstimateCalculatorTest {

    private static final PriceList ALFA = new PriceList(
            money("40.00"), money("20"), money("5"), money("21"),
            Map.of(CarClass.LENGVASIS, money("1.00"),
                    CarClass.VISUREIGIS, money("1.20"),
                    CarClass.MIKROAUTOBUSAS, money("1.30")),
            Map.of("D01", work("D01", "Priekinių stabdžių kaladėlių keitimas", "1.0", "PRIEKINIAI_RATAI", "0.4", null),
                    "D02", work("D02", "Priekinių stabdžių diskų keitimas", "1.6", "PRIEKINIAI_RATAI", "0.4", null),
                    "D03", work("D03", "Variklio alyvos ir filtro keitimas", "0.5", null, null, null),
                    "D05", work("D05", "Kompiuterinė diagnostika", "0.5", null, null, "20")));

    private final EstimateCalculator calculator = new EstimateCalculator(
            new EstimateRequestValidator(), List.of(new PromotionDiscount(), new RegularCustomerDiscount()));

    @Test
    void normalCase_calculatesLabourPartsAndVat() {
        // Arrange
        EstimateRequest request = oilAndPadsRequest(null);

        // Act
        Estimate estimate = calculator.calculate(request, ALFA);

        // Assert
        assertThat(estimate.laborTotal()).isEqualByComparingTo("60.00");
        assertThat(estimate.partsTotal()).isEqualByComparingTo("82.20");
        assertThat(estimate.subtotal()).isEqualByComparingTo("142.20");
        assertThat(estimate.vat()).isEqualByComparingTo("29.86");
        assertThat(estimate.total()).isEqualByComparingTo("172.06");
        assertThat(estimate.status()).isEqualTo(EstimateStatus.PARENGTA);
    }

    @Test
    void boundary_totalEqualToPriceLimit_isReady() {
        EstimateRequest request = oilAndPadsRequest(money("172.06"));

        Estimate estimate = calculator.calculate(request, ALFA);

        assertThat(estimate.total()).isEqualByComparingTo("172.06");
        assertThat(estimate.status()).isEqualTo(EstimateStatus.PARENGTA);
        assertThat(estimate.overLimitBy()).isNull();
    }

    @Test
    void conflict_sharedOperationIsChargedOnce_largestDiscountWins_limitExceeded() {
        EstimateRequest request = new EstimateRequest("VISUREIGIS", true, List.of("D01", "D02", "D05"),
                List.of(part("Stabdžių kaladėlės", "30.00", "1"), part("Stabdžių diskai", "45.00", "2")),
                money("250.00"));

        Estimate estimate = calculator.calculate(request, ALFA);

        List<Estimate.LaborLine> lines = estimate.laborLines();
        // R1 + R2: D01 loses the shared 0.4 h -> 0.6 h × 1.2 = 0.72 -> 0.8 h; D02 1.6 × 1.2 = 1.92 -> 2.0 h
        assertThat(lines).extracting(Estimate.LaborLine::billedHours)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(money("0.8"), money("2.0"), money("0.6"));
        // R4: 5 % regular-customer discount, except D05 where the 20 % promotion is larger (not 25 %)
        assertThat(lines).extracting(Estimate.LaborLine::finalPrice)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactly(money("30.40"), money("76.00"), money("19.20"));
        assertThat(estimate.partsTotal()).isEqualByComparingTo("144.00");
        assertThat(estimate.subtotal()).isEqualByComparingTo("269.60");
        assertThat(estimate.vat()).isEqualByComparingTo("56.62");
        assertThat(estimate.total()).isEqualByComparingTo("326.22");
        assertThat(estimate.status()).isEqualTo(EstimateStatus.LAUKIA_PATVIRTINIMO);
        assertThat(estimate.overLimitBy()).isEqualByComparingTo("76.22");
    }

    @Test
    void rounding_halfCentRoundsUp() {
        // part 0.42 × 1.2 = 0.504 -> 0.50; subtotal 20.50; VAT 20.50 × 21 % = 4.305 -> 4.31 (HALF_EVEN would give 4.30)
        EstimateRequest request = new EstimateRequest("LENGVASIS", false, List.of("D03"),
                List.of(part("Tarpinė", "0.42", "1")), null);

        Estimate estimate = calculator.calculate(request, ALFA);

        assertThat(estimate.partsTotal()).isEqualByComparingTo("0.50");
        assertThat(estimate.subtotal()).isEqualByComparingTo("20.50");
        assertThat(estimate.vat()).isEqualByComparingTo("4.31");
        assertThat(estimate.total()).isEqualByComparingTo("24.81");
    }

    @Test
    void error_otherWorkshopCodeAndZeroQuantity_rejectedWithBothMessages() {
        EstimateRequest request = new EstimateRequest("LENGVASIS", false, List.of("D01", "B03"),
                List.of(part("Stabdžių kaladėlės", "30.00", "0")), null);

        assertThatThrownBy(() -> calculator.calculate(request, ALFA))
                .isInstanceOfSatisfying(InvalidEstimateException.class, error ->
                        assertThat(error.getErrors()).containsExactly(
                                "Nežinomas darbo kodas: B03",
                                "Detalės „Stabdžių kaladėlės“ kiekis turi būti didesnis už 0"));
    }

    private static EstimateRequest oilAndPadsRequest(BigDecimal priceLimit) {
        return new EstimateRequest("LENGVASIS", false, List.of("D01", "D03"),
                List.of(part("Stabdžių kaladėlės", "30.00", "1"),
                        part("Alyva 5W-30, l", "8.00", "4"),
                        part("Alyvos filtras", "6.50", "1")),
                priceLimit);
    }

    private static WorkItem work(String code, String name, String hours,
                                 String sharedOperation, String sharedHours, String promotion) {
        return new WorkItem(code, name, money(hours), sharedOperation,
                sharedHours == null ? null : money(sharedHours), promotion == null ? null : money(promotion));
    }

    private static PartRequest part(String name, String unitCost, String quantity) {
        return new PartRequest(name, money(unitCost), money(quantity));
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
