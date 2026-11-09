package lt.samata.persistence;

import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface WorkshopRepository extends Repository<Workshop, String> {

    Optional<Workshop> findById(String id);
}
