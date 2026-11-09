package lt.samata.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.samata.domain.Estimate;
import lt.samata.domain.EstimateCalculator;
import lt.samata.domain.EstimateRequest;
import lt.samata.domain.PriceList;
import lt.samata.domain.WorkItem;
import lt.samata.persistence.EstimateEntity;
import lt.samata.persistence.EstimateRepository;
import lt.samata.persistence.WorkItemRepository;
import lt.samata.persistence.Workshop;
import lt.samata.persistence.WorkshopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Connects the steps of a use case: load the workshop's price list, run the core module, save the result.
 * Every method takes the workshop id first, and every lookup is filtered by it (tenant isolation).
 */
@Service
@Transactional
public class EstimateService {

    private final WorkshopRepository workshops;
    private final WorkItemRepository workItems;
    private final EstimateRepository estimates;
    private final EstimateCalculator calculator;
    private final ObjectMapper objectMapper;

    public EstimateService(WorkshopRepository workshops, WorkItemRepository workItems,
                           EstimateRepository estimates, EstimateCalculator calculator, ObjectMapper objectMapper) {
        this.workshops = workshops;
        this.workItems = workItems;
        this.estimates = estimates;
        this.calculator = calculator;
        this.objectMapper = objectMapper;
    }

    public EstimateResponse create(String workshopId, EstimateRequest request) {
        Estimate estimate = calculator.calculate(request, loadPriceList(workshopId));
        EstimateEntity saved = estimates.save(
                new EstimateEntity(workshopId, estimate.status(), estimate.total(), toJson(estimate)));
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EstimateResponse get(String workshopId, Long estimateId) {
        return toResponse(findEstimate(workshopId, estimateId));
    }

    @Transactional(readOnly = true)
    public List<EstimateResponse> list(String workshopId) {
        findWorkshop(workshopId);
        return estimates.findByWorkshopIdOrderByIdDesc(workshopId).stream().map(this::toResponse).toList();
    }

    public EstimateResponse approve(String workshopId, Long estimateId) {
        EstimateEntity estimate = findEstimate(workshopId, estimateId);
        estimate.approve();
        return toResponse(estimate);
    }

    @Transactional(readOnly = true)
    public List<WorkItem> listWorks(String workshopId) {
        return loadPriceList(workshopId).works().values().stream()
                .sorted(Comparator.comparing(WorkItem::code))
                .toList();
    }

    private PriceList loadPriceList(String workshopId) {
        return findWorkshop(workshopId).toPriceList(workItems.findByWorkshopId(workshopId));
    }

    private Workshop findWorkshop(String workshopId) {
        return workshops.findById(workshopId)
                .orElseThrow(() -> new NotFoundException("Nežinomas servisas: " + workshopId));
    }

    private EstimateEntity findEstimate(String workshopId, Long estimateId) {
        findWorkshop(workshopId);
        // another workshop's estimate gets the same answer as a missing one, so its existence is not revealed
        return estimates.findByIdAndWorkshopId(estimateId, workshopId)
                .orElseThrow(() -> new NotFoundException("Sąmata nerasta: " + estimateId));
    }

    private EstimateResponse toResponse(EstimateEntity entity) {
        Estimate details = fromJson(entity.getDetailsJson()).withStatus(entity.getStatus());
        return new EstimateResponse(entity.getId(), entity.getCreatedAt(), details);
    }

    private String toJson(Estimate estimate) {
        try {
            return objectMapper.writeValueAsString(estimate);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Nepavyko išsaugoti sąmatos", e);
        }
    }

    private Estimate fromJson(String json) {
        try {
            return objectMapper.readValue(json, Estimate.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Sugadinti sąmatos duomenys", e);
        }
    }
}
