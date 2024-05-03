package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import project.model.image.Image;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends MongoRepository<Image, String> {

    Optional<List<Image>> findAllByName(String name);
    Optional<List<Image>> findAllByDatasetId(String datasetId);

    Optional<List<Image>> deleteAllByDatasetId(String datasetId);

    @Query("{ 'categories': ?0 }")
    Optional<List<Image>>  findByCategoryContains(String category);

    @Query("{ 'categories': { $all: ?0 } }")
    Optional<List<Image>> findByCategoriesContainingAll(List<String> categories);
    @Query("{ 'categories': { $all: ?0 }, 'datasetId': ?1 }")
    Optional<List<Image>>findByCategoriesContainingAllAndDatasetId(List<String> categories, String datasetId);



}
