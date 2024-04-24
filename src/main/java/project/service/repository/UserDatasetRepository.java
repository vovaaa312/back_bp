package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import project.model.dataset.UserDataset;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDatasetRepository extends MongoRepository<UserDataset, String> {
    Optional <List<UserDataset>> findAllByUserId(String id);
   Optional <List<UserDataset>> findAllByDatasetId(String id);
    Optional<UserDataset> findById(String id);

  //  @Query("{'datasetId' : ?0, 'userId' : ?1}")
  Optional<UserDataset> findFirstByDatasetIdAndUserId(String datasetId, String userId);


    Optional<List<UserDataset>> deleteUserDatasetsByDatasetId(String datasetId);

}
