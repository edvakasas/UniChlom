package lt.samata.domain;

import lt.samata.domain.Estimate.LaborLine;
import lt.samata.domain.Estimate.PartLine;
import lt.samata.domain.discount.DiscountContext;
import lt.samata.domain.discount.DiscountRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The core module: turns a workshop price list and a request into an estimate (rules R1–R8).
 * Plain Java with no Spring or database code, so every rule is unit tested directly.
 */
public class EstimateCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** R1: the job with the most norm hours keeps a shared operation; on a tie, the smaller code keeps it. */
    private static final Comparator<WorkItem> KEEPS_SHARED_OPERATION =
            Comparator.comparing(WorkItem::normHours).thenComparing(WorkItem::code, Comparator.reverseOrder());

    private final EstimateRequestValidator validator;
    private final List<DiscountRule> discountRules;

    public EstimateCalculator(EstimateRequestValidator validator, List<DiscountRule> discountRules) {
        this.validator = validator;
        this.discountRules = List.copyOf(discountRules);
    }

    public Estimate calculate(EstimateRequest request, PriceList priceList) {
        validator.validate(request, priceList);

        List<WorkItem> works = request.workCodes().stream()
                .map(code -> priceList.findWork(code).orElseThrow())
                .toList();
        BigDecimal coefficient = priceList.coefficientFor(CarClass.valueOf(request.carClass()));
        Map<String, BigDecimal> adjustedHours = hoursAfterSharedOperations(works);
        Map<String, BigDecimal> billedHours = new HashMap<>();
        works.forEach(work -> billedHours.put(work.code(), billableHours(adjustedHours.get(work.code()), coefficient)));

        DiscountContext context = new DiscountContext(request, priceList, sum(billedHours.values().stream().toList()));
        List<LaborLine> laborLines = works.stream()
                .map(work -> laborLine(work, billedHours.get(work.code()), context))
                .toList();
        List<PartLine> partLines = request.parts().stream()
                .map(part -> partLine(part, priceList.partsMarkupPercent()))
                .toList();

        return summarize(laborLines, partLines, priceList.vatPercent(), request.priceLimit());
    }

    /** R1: a shared operation is charged once; every other job that includes it loses its hours. */
    private Map<String, BigDecimal> hoursAfterSharedOperations(List<WorkItem> works) {
        Map<String, BigDecimal> hours = new HashMap<>();
        works.forEach(work -> hours.put(work.code(), work.normHours()));

        Map<String, List<WorkItem>> worksByOperation = works.stream()
                .filter(WorkItem::hasSharedOperation)
                .collect(Collectors.groupingBy(WorkItem::sharedOperation));
        for (List<WorkItem> sharing : worksByOperation.values()) {
            WorkItem keeper = sharing.stream().max(KEEPS_SHARED_OPERATION).orElseThrow();
            sharing.stream()
                    .filter(work -> work != keeper)
                    .forEach(work -> hours.put(work.code(), work.normHours().subtract(work.sharedOperationHours())));
        }
        return hours;
    }

    /** R2: hours × car class coefficient, rounded up to 0.1 h. */
    private BigDecimal billableHours(BigDecimal hours, BigDecimal coefficient) {
        return hours.multiply(coefficient).setScale(1, RoundingMode.CEILING);
    }

    /** R3 + R4: price of the billed hours, minus the single largest discount any rule gives. */
    private LaborLine laborLine(WorkItem work, BigDecimal billedHours, DiscountContext context) {
        BigDecimal price = Money.round(billedHours.multiply(context.priceList().hourlyRate()));
        BigDecimal discountPercent = discountRules.stream()
                .flatMap(rule -> rule.percentFor(work, context).stream())
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal discount = Money.percentOf(price, discountPercent);
        return new LaborLine(work.code(), work.name(), billedHours, price, discountPercent, discount,
                price.subtract(discount));
    }

    /** R5: purchase price + markup, times quantity. No discounts on parts. */
    private PartLine partLine(EstimateRequest.PartRequest part, BigDecimal markupPercent) {
        BigDecimal price = Money.round(part.unitCost()
                .multiply(HUNDRED.add(markupPercent))
                .multiply(part.quantity())
                .divide(HUNDRED));
        return new PartLine(part.name(), part.unitCost(), part.quantity(), price);
    }

    /** R6 + R7: totals with VAT, then the status against the customer's price limit. */
    private Estimate summarize(List<LaborLine> laborLines, List<PartLine> partLines,
                               BigDecimal vatPercent, BigDecimal priceLimit) {
        BigDecimal laborTotal = sum(laborLines.stream().map(LaborLine::finalPrice).toList());
        BigDecimal partsTotal = sum(partLines.stream().map(PartLine::price).toList());
        BigDecimal subtotal = laborTotal.add(partsTotal);
        BigDecimal vat = Money.percentOf(subtotal, vatPercent);
        BigDecimal total = subtotal.add(vat);

        boolean overLimit = priceLimit != null && total.compareTo(priceLimit) > 0;
        EstimateStatus status = overLimit ? EstimateStatus.LAUKIA_PATVIRTINIMO : EstimateStatus.PARENGTA;
        BigDecimal overLimitBy = overLimit ? total.subtract(priceLimit) : null;
        return new Estimate(laborLines, partLines, laborTotal, partsTotal, subtotal, vat, total, status, overLimitBy);
    }

    private static BigDecimal sum(List<BigDecimal> amounts) {
        return amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
