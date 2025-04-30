package kaixin.learning.mongodb.repository;

import kaixin.learning.mongodb.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    @Query(value = "{'roles.roleId': ?0}", count = true)
    long countByRoleId(String roleId);
}