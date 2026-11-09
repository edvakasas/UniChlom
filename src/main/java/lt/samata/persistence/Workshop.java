package lt.samata.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lt.samata.domain.CarClass;
import lt.samata.domain.PriceList;
import lt.samata.domain.WorkItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** A tenant: one car workshop with its own rates. Its id (e.g. "alfa") is what the X-Tenant-Id header carries. */
@Entity
public class Workshop {

    @Id
    private String id;
    private String name;
    private BigDecimal hourlyRate;
    private BigDecimal partsMarkupPercent;
    private BigDecimal regularCustomerDiscountPercent;
    private BigDecimal vatPercent;
    private BigDecimal coefficientLengvasis;
    private BigDecimal coefficientVisureigis;
    private BigDecimal coefficientMikroautobusas;

    protected Workshop() {
    }

    public String getId() {
        return id;
    }

    public PriceList toPriceList(List<WorkItemEntity> workEntities) {
        Map<String, WorkItem> works = workEntities.stream()
                .map(WorkItemEntity::toWorkItem)
                .collect(Collectors.toMap(WorkItem::code, Function.identity()));
        return new PriceList(hourlyRate, partsMarkupPercent, regularCustomerDiscountPercent, vatPercent,
                Map.of(CarClass.LENGVASIS, coefficientLengvasis,
                        CarClass.VISUREIGIS, coefficientVisureigis,
                        CarClass.MIKROAUTOBUSAS, coefficientMikroautobusas),
                works);
    }
}
