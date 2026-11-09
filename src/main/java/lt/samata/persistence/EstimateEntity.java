package lt.samata.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lt.samata.domain.EstimateStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A saved estimate. The calculated lines never change after saving, so they are stored as one JSON document;
 * only the status changes (when the customer approves it).
 */
@Entity
@Table(name = "estimate")
public class EstimateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String workshopId;

    @Enumerated(EnumType.STRING)
    private EstimateStatus status;

    private BigDecimal total;

    @Lob
    private String detailsJson;

    private Instant createdAt;

    protected EstimateEntity() {
    }

    public EstimateEntity(String workshopId, EstimateStatus status, BigDecimal total, String detailsJson) {
        this.workshopId = workshopId;
        this.status = status;
        this.total = total;
        this.detailsJson = detailsJson;
        this.createdAt = Instant.now();
    }

    /** Only an estimate that is over the customer's limit needs, and can get, approval. */
    public void approve() {
        if (status != EstimateStatus.LAUKIA_PATVIRTINIMO) {
            throw new EstimateStateException("Patvirtinti galima tik sąmatą, kurios būsena LAUKIA_PATVIRTINIMO; dabar: " + status);
        }
        status = EstimateStatus.PATVIRTINTA;
    }

    public Long getId() {
        return id;
    }

    public EstimateStatus getStatus() {
        return status;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
