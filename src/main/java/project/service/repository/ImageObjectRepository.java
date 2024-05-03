package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import project.model.image.ImageObject;

import java.util.*;

@Repository
public interface ImageObjectRepository  extends MongoRepository<ImageObject,String> {
    List<ImageObject> findAllByImageId(String id);

    List<ImageObject>deleteAllByImageId (String id);

}
