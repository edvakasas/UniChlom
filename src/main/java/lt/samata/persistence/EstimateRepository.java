package lt.samata.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Extends Spring's bare {@link Repository}, not JpaRepository, on purpose: there is no findById or findAll
 * without a workshop, so code cannot read another workshop's estimates by accident.
 */
public interface EstimateRepository extends Repository<EstimateEntity, Long> {

    EstimateEntity save(EstimateEntity estimate);

    Optional<EstimateEntity> findByIdAndWorkshopId(Long id, String workshopId);

    List<EstimateEntity> findByWorkshopIdOrderByIdDesc(String workshopId);
}
