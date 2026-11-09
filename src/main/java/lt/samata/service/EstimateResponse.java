package lt.samata.service;

import lt.samata.domain.Estimate;

import java.time.Instant;

public record EstimateResponse(Long id, Instant createdAt, Estimate estimate) {
}
