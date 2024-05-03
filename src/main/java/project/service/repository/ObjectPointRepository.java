package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import project.model.image.ObjectPoint;

import java.util.*;

@Repository
public interface ObjectPointRepository extends MongoRepository<ObjectPoint, String> {
    Optional<List<ObjectPoint>> findAllByImageObjectId(String id);

    Optional<List<ObjectPoint>> deleteAllByImageObjectId(String id);

    //Optional<ObjectPoint> findFirstByXAndYAndObjectId(String x, String y);



}
