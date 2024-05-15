package project.service.repository;

import org.bouncycastle.LICENSE;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import project.model.user.AuthUser;

import java.util.List;
import java.util.Optional;
@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {


    //@Query("{email:?0}")
    Optional<AuthUser> findAuthUserByEmail(String email);
    Optional<AuthUser> findAuthUserByUsername(String username);

    Optional<List<AuthUser>> findAllById(String id);

    Optional<AuthUser> findAuthUsersById(String id);






}