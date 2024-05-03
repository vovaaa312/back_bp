package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import project.model.dataset.Dataset;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatasetRepository extends MongoRepository<Dataset, String> {
    Optional<List<Dataset>> findAllByName(String name);

    Optional<List<Dataset>>findAllByOwnerId(String ownerId);
    Optional<List<Dataset>> findDatasetsByProjectId(String projectId);
    Optional<List<Dataset>> deleteAllByProjectId(String projectId);

    Optional<List<Dataset>> findAllByCategory(String category);

    Optional<List<Dataset>> findAllByCategoryAndProjectId(String category, String projectId);



}
