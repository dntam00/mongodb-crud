package kaixin.learning.mongodb.repository;

import kaixin.learning.mongodb.model.Role;
import kaixin.learning.mongodb.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRoleRepository {

    private final MongoTemplate mongoTemplate;
    private final RoleRepository roleRepository;

    public boolean removeRolePermanently(String roleId) {
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isEmpty()) {
            return false;
        }

        // Use MongoTemplate to remove the role from all users in a single operation
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("roles.roleId").is(roleId)),
                new Update().pull("roles", Query.query(Criteria.where("roleId").is(roleId))),
                User.class
        );

        roleRepository.deleteById(roleId);
        return true;
    }
}
