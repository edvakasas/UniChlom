package lt.samata.web;

import lt.samata.domain.EstimateRequest;
import lt.samata.domain.WorkItem;
import lt.samata.service.EstimateResponse;
import lt.samata.service.EstimateService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP API. The workshop (tenant) comes from the X-Tenant-Id header and is passed into every service call.
 * In stage III it will come from the logged-in user instead.
 */
@RestController
@RequestMapping("/api")
public class EstimateController {

    static final String TENANT_HEADER = "X-Tenant-Id";

    private final EstimateService service;

    public EstimateController(EstimateService service) {
        this.service = service;
    }

    @GetMapping("/works")
    public List<WorkItem> listWorks(@RequestHeader(TENANT_HEADER) String workshopId) {
        return service.listWorks(workshopId);
    }

    @PostMapping("/estimates")
    @ResponseStatus(HttpStatus.CREATED)
    public EstimateResponse create(@RequestHeader(TENANT_HEADER) String workshopId,
                                   @RequestBody EstimateRequest request) {
        return service.create(workshopId, request);
    }

    @GetMapping("/estimates")
    public List<EstimateResponse> list(@RequestHeader(TENANT_HEADER) String workshopId) {
        return service.list(workshopId);
    }

    @GetMapping("/estimates/{id}")
    public EstimateResponse get(@RequestHeader(TENANT_HEADER) String workshopId, @PathVariable Long id) {
        return service.get(workshopId, id);
    }

    @PostMapping("/estimates/{id}/approve")
    public EstimateResponse approve(@RequestHeader(TENANT_HEADER) String workshopId, @PathVariable Long id) {
        return service.approve(workshopId, id);
    }
}
