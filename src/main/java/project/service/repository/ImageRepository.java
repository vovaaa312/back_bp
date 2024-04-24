package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import project.model.image.Image;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends MongoRepository<Image, String> {

    Optional<List<Image>> findAllByName(String name);
    Optional<List<Image>> findAllByDatasetId(String datasetId);


}
