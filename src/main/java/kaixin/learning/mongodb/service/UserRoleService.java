package kaixin.learning.mongodb.service;

import kaixin.learning.mongodb.model.Role;
import kaixin.learning.mongodb.model.User;
import kaixin.learning.mongodb.model.UserWithRoles;
import kaixin.learning.mongodb.repository.RoleRepository;
import kaixin.learning.mongodb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final MongoTemplate mongoTemplate;

    public void assignRoleToUser(String userId, String roleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));

        // Add role reference to user
        user.getRoles().add(new User.RoleReference(role.getId(), role.getName()));

        // Add user reference to role
        role.getUsers().add(new Role.UserReference(user.getId(), user.getUsername()));

        // Save both documents
        userRepository.save(user);
        roleRepository.save(role);
    }

    public void removeRoleFromUser(String userId, String roleId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Role role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));

        // Remove role reference from user
        user.getRoles().removeIf(roleRef -> roleRef.getRoleId().equals(roleId));

        // Remove user reference from role
        role.getUsers().removeIf(userRef -> userRef.getUserId().equals(userId));

        // Save both documents
        userRepository.save(user);
        roleRepository.save(role);
    }

    /**
     * Permanently removes a role from the system.
     * This will also remove all references to this role from users.
     *
     * @param roleId The ID of the role to be removed
     * @return true if successful, false if role not found
     */
    public boolean removeRolePermanently(String roleId) {
        // Find the role
        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isEmpty()) {
            return false;
        }

        Role role = optionalRole.get();

        // Get all users who have this role
        Set<Role.UserReference> userRefs = new HashSet<>(role.getUsers());

        // Remove the role reference from each user
        for (Role.UserReference userRef : userRefs) {
            Optional<User> optionalUser = userRepository.findById(userRef.getUserId());
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.getRoles().removeIf(roleRef -> roleRef.getRoleId().equals(roleId));
                userRepository.save(user);
            }
        }

        // Finally delete the role
        roleRepository.delete(role);
        return true;
    }

    /**
     * Finds a user by username and fetches their roles in a single query using aggregation
     *
     * @param username The username to search for
     * @return User with populated roles, or empty if not found
     */
    public Optional<UserWithRoles> findByUsernameWithRoles(String username) {
        // Match stage to find the user by username
        MatchOperation matchStage = Aggregation.match(
                Criteria.where("username").is(username)
        );

        // Lookup stage to join with roles collection
        LookupOperation lookupStage = Aggregation.lookup(
                "roles",         // from collection
                "refRoles",     // local field (path to roleId in the embedded roles array)
                "_id",           // foreign field
                "tempRoles"      // temporary output array for the full role objects
        );


        ProjectionOperation project = Aggregation.project()
                                                 .and("id").as("id")
                                                 .and("username").as("username")
                                                 .and("tempRoles").as("roles");  // Replace roles[] with roleDetails[]


        // Build aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                lookupStage,
                project
        );

        log.info("aggregation: {}", aggregation);

        // Execute aggregation
        AggregationResults<UserWithRoles> results = mongoTemplate.aggregate(
                aggregation,
                "users",
                UserWithRoles.class  // Custom DTO to hold the result
        );

        return Optional.ofNullable(results.getUniqueMappedResult());
    }
}