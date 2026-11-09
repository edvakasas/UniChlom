package lt.samata.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

public interface WorkItemRepository extends Repository<WorkItemEntity, Long> {

    List<WorkItemEntity> findByWorkshopId(String workshopId);
}
