package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import project.model.project.Project;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<List<Project>> findProjectsByOwnerId(String ownerId);
  //  List<Project> findProjectsBy(String ownerId);

}
