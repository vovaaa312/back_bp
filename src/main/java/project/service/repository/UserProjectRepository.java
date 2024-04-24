package project.service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import project.model.project.UserProject;

import java.util.List;
import java.util.Optional;

@Repository

public interface UserProjectRepository extends MongoRepository<UserProject, String> {
    Optional<List<UserProject>> findUserProjectsByProjectId(String projectId);

    Optional<List<UserProject>>findUserProjectsByUserId(String userId);

    Optional<List<UserProject>> deleteAllByProjectId(String projectId);

    //@Query("{'projectId' : ?0, 'userId' : ?1}")
    Optional<UserProject> findFirstByProjectIdAndUserId(String projectId, String userId);
    // deleteAllByProjectId(String projectId);


}
