package lt.samata.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.samata.domain.WorkItem;

import java.math.BigDecimal;

/** A row of a workshop's price list; always belongs to exactly one workshop. */
@Entity
@Table(name = "work_item")
public class WorkItemEntity {

    @Id
    private Long id;
    private String workshopId;
    private String code;
    private String name;
    private BigDecimal normHours;
    private String sharedOperation;
    private BigDecimal sharedOperationHours;
    private BigDecimal promotionPercent;

    protected WorkItemEntity() {
    }

    public WorkItem toWorkItem() {
        return new WorkItem(code, name, normHours, sharedOperation, sharedOperationHours, promotionPercent);
    }
}
