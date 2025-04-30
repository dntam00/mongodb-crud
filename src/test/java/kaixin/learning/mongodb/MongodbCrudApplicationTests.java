package kaixin.learning.mongodb;

import kaixin.learning.mongodb.model.*;
import kaixin.learning.mongodb.repository.ProductRepository;
import kaixin.learning.mongodb.repository.RoleRepository;
import kaixin.learning.mongodb.repository.UserRepository;
import kaixin.learning.mongodb.repository.UserRoleRepository;
import kaixin.learning.mongodb.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductRepositoryTests {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testCreateAndLoadDifferentProductTypes() {
        // Clean up any existing data
        productRepository.deleteAll();

        // Create an electronic product
        ElectronicProduct laptop = new ElectronicProduct(
                "Laptop Pro",
                "High performance laptop for developers",
                1299.99,
                "TechBrand",
                "Developer X5",
                24
        );

        // Create a clothing product
        ClothingProduct tshirt = new ClothingProduct(
                "Casual T-Shirt",
                "Comfortable cotton t-shirt for daily wear",
                29.99,
                "L",
                "Black",
                "100% Cotton"
        );

        // Save both products
        ElectronicProduct savedLaptop = (ElectronicProduct) productRepository.save(laptop);
        ClothingProduct savedTshirt = (ClothingProduct) productRepository.save(tshirt);

        // Verify products were saved with IDs
        assertNotNull(savedLaptop.getId());
        assertNotNull(savedTshirt.getId());

        // Load electronic product and verify specific properties
        Optional<Product> retrievedLaptopOpt = productRepository.findById(savedLaptop.getId());
        assertTrue(retrievedLaptopOpt.isPresent());

        ElectronicProduct retrievedLaptop = (ElectronicProduct) retrievedLaptopOpt.get();
        assertEquals("ELECTRONIC", retrievedLaptop.getProductType());
        assertEquals("TechBrand", retrievedLaptop.getBrand());
        assertEquals("Developer X5", retrievedLaptop.getModel());
        assertEquals(24, retrievedLaptop.getWarrantyMonths());

        // Load clothing product and verify specific properties
        Optional<Product> retrievedTshirtOpt = productRepository.findById(savedTshirt.getId());
        assertTrue(retrievedTshirtOpt.isPresent());

        ClothingProduct retrievedTshirt = (ClothingProduct) retrievedTshirtOpt.get();
        assertEquals("CLOTHING", retrievedTshirt.getProductType());
        assertEquals("L", retrievedTshirt.getSize());
        assertEquals("Black", retrievedTshirt.getColor());
        assertEquals("100% Cotton", retrievedTshirt.getMaterial());

        // Test finding by product type
        List<Product> electronicProducts = productRepository.findByProductType("ELECTRONIC");
        assertEquals(1, electronicProducts.size());

        List<Product> clothingProducts = productRepository.findByProductType("CLOTHING");
        assertEquals(1, clothingProducts.size());

        // Verify total count
        assertEquals(2, productRepository.count());
    }
}

@SpringBootTest
@Slf4j
@RequiredArgsConstructor
class UserRoleRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRoleService userRoleService;

    private final int USER_COUNT = 100000;
    private final int ROLE_COUNT = 20;
    private final int ROLES_PER_USER = 3;

    @BeforeEach
    public void before() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }


    @Test
    void testSearchUserWithRole() {
        List<Role> roles = createRoles(ROLE_COUNT);

        int userId = 1;

        User user = buildUser(userId);

        Role selectedRole = roles.get(0);
        selectedRole.getUsers().add(new Role.UserReference(user.getId(), user.getUsername()));
        user.getRoles().add(new User.RoleReference(selectedRole.getId(), selectedRole.getName()));

        userRepository.save(user);

        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        assertTrue(byUsername.isPresent());
        User foundUser = byUsername.get();
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getUsername(), foundUser.getUsername());
        assertEquals(user.getEmail(), foundUser.getEmail());
        assertEquals(user.getRoles().size(), foundUser.getRoles().size());
    }

    @Test
    void testRemoveRolePermanently() {
        // Create roles first
        List<Role> roles = createRoles(ROLE_COUNT);

        // Create users with multiple roles
        createUsers(USER_COUNT, roles);

        // Select a role to delete (pick the middle one)
        String roleToDelete = roles.get(roles.size() / 2).getId();
        String roleName = roles.get(roles.size() / 2).getName();

        // Count how many users have this role before deletion
        long usersWithRoleBefore = mongoTemplate.count(
                Query.query(Criteria.where("roles.roleId").is(roleToDelete)),
                User.class
        );

        log.info("Users with role {} before deletion: ", usersWithRoleBefore);
        assertTrue(usersWithRoleBefore > 0);

        // Execute and time the role deletion
        log.info("Starting deletion of role: {}", roleName);
        long startTime = System.currentTimeMillis();

        boolean result = userRoleRepository.removeRolePermanently(roleToDelete);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("Role deletion completed in {} ms", duration);

        // Verify role was successfully deleted
        assertTrue(result);
        assertFalse(roleRepository.findById(roleToDelete).isPresent());

        // Verify no user still has the deleted role
        long usersWithRoleAfter = mongoTemplate.count(
                Query.query(Criteria.where("roles.roleId").is(roleToDelete)),
                User.class
        );

        assertEquals(0, usersWithRoleAfter);
    }

    @Test
    void testFindByUsernameWithRoles() {
        // Create some roles
        List<Role> roles = createRoles(3);
        Role role1 = roles.get(0);
        Role role2 = roles.get(1);

        // Create a test user with roles
        User user = buildUser(999);

        // Save the user first to get an ID
        user = userRepository.save(user);

        // Add roles to the user
        user.getRefRoles().add(new ObjectId(role1.getId()));
        user.getRefRoles().add(new ObjectId(role2.getId()));

        // Save the updated user
        user = userRepository.save(user);

        // Add user references to the roles
        role1.getUsers().add(new Role.UserReference(user.getId(), user.getUsername()));
        role2.getUsers().add(new Role.UserReference(user.getId(), user.getUsername()));
        roleRepository.saveAll(List.of(role1, role2));

        // Call the method to fetch the user with roles
        Optional<UserWithRoles> result = userRoleService.findByUsernameWithRoles(user.getUsername());

        // Assert user is found
        assertTrue(result.isPresent());
        UserWithRoles foundUser = result.get();

        // Verify basic user info
        assertEquals(user.getId(), foundUser.getId());
        assertEquals(user.getUsername(), foundUser.getUsername());

        // Verify role references are populated
        assertNotNull(foundUser.getRoles());
        assertEquals(2, foundUser.getRoles().size());
    }

    @Test
    void testFindByRole() {
        // Create multiple roles
        List<Role> roles = createRoles(5);

        // Select specific roles to search for later
        String roleName0 = roles.get(0).getName();

        // Create multiple users with different role combinations
        User user1 = buildUser(1);
        User user2 = buildUser(2);
        User user3 = buildUser(3);

        // Save users to get IDs
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);

        // Assign roles to users
        // User1 gets role 0
        user1.getRoles().add(new User.RoleReference(roles.get(0).getId(), roles.get(0).getName()));

        // User2 gets roles 1 and 2
        user2.getRoles().add(new User.RoleReference(roles.get(1).getId(), roles.get(1).getName()));
        user2.getRoles().add(new User.RoleReference(roles.get(2).getId(), roles.get(2).getName()));

        // User3 gets roles 3 and 4
        user3.getRoles().add(new User.RoleReference(roles.get(3).getId(), roles.get(3).getName()));
        user3.getRoles().add(new User.RoleReference(roles.get(4).getId(), roles.get(4).getName()));

        // Save all users with their role assignments
        userRepository.saveAll(List.of(user1, user2, user3));

        // Test searching for users with specific role names
        List<User> foundUsers = userRepository.findByContainRole(roleName0);

        // Should find user1 (has role0) and user2 (has role2)
        assertEquals(1, foundUsers.size());

        // Verify we found the correct users
        List<String> foundUsernames = foundUsers.stream()
                                                .map(User::getUsername)
                                                .toList();

        assertTrue(foundUsernames.contains(user1.getUsername()));
        assertFalse(foundUsernames.contains(user2.getUsername()));
        assertFalse(foundUsernames.contains(user3.getUsername()));

        // Test with a role name that no user has
        List<User> noUsers = userRepository.findByAnyRoleIn(List.of("NON_EXISTENT_ROLE"));
        assertTrue(noUsers.isEmpty());

        // Test with empty list
        List<User> emptySearch = userRepository.findByAnyRoleIn(List.of());
        assertTrue(emptySearch.isEmpty());
    }

    @Test
    void testFindByAnyRoleIdIn() {
        // Create multiple roles
        List<Role> roles = createRoles(5);

        // Select specific roles to search for later
        String roleName0 = roles.get(0).getName();
        String roleName2 = roles.get(2).getName();

        // Create multiple users with different role combinations
        User user1 = buildUser(1);
        User user2 = buildUser(2);
        User user3 = buildUser(3);

        // Save users to get IDs
        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
        user3 = userRepository.save(user3);

        // Assign roles to users
        // User1 gets role 0
        user1.getRoles().add(new User.RoleReference(roles.get(0).getId(), roles.get(0).getName()));

        // User2 gets roles 1 and 2
        user2.getRoles().add(new User.RoleReference(roles.get(1).getId(), roles.get(1).getName()));
        user2.getRoles().add(new User.RoleReference(roles.get(2).getId(), roles.get(2).getName()));

        // User3 gets roles 3 and 4
        user3.getRoles().add(new User.RoleReference(roles.get(3).getId(), roles.get(3).getName()));
        user3.getRoles().add(new User.RoleReference(roles.get(4).getId(), roles.get(4).getName()));

        // Save all users with their role assignments
        userRepository.saveAll(List.of(user1, user2, user3));

        // Test searching for users with specific role names
        List<String> searchRoleNames = List.of(roleName0, roleName2);
        List<User> foundUsers = userRepository.findByAnyRoleIn(searchRoleNames);

        // Should find user1 (has role0) and user2 (has role2)
        assertEquals(2, foundUsers.size());

        // Verify we found the correct users
        List<String> foundUsernames = foundUsers.stream()
                                                .map(User::getUsername)
                                                .toList();

        assertTrue(foundUsernames.contains(user1.getUsername()));
        assertTrue(foundUsernames.contains(user2.getUsername()));
        assertFalse(foundUsernames.contains(user3.getUsername()));

        // Test with a role name that no user has
        List<User> noUsers = userRepository.findByAnyRoleIn(List.of("NON_EXISTENT_ROLE"));
        assertTrue(noUsers.isEmpty());

        // Test with empty list
        List<User> emptySearch = userRepository.findByAnyRoleIn(List.of());
        assertTrue(emptySearch.isEmpty());
    }



    private List<Role> createRoles(int count) {
        System.out.println("Creating " + count + " roles...");
        List<Role> roles = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Role role = new Role();
            role.setName("ROLE_" + i);
            role.setDescription("Description for role " + i);
            role.setUsers(new java.util.HashSet<>());
            roles.add(role);
        }

        return roleRepository.saveAll(roles);
    }

    private void createUsers(int count, List<Role> roles) {
        System.out.println("Creating " + count + " users with multiple roles...");
        Random random = new Random(42); // Fixed seed for reproducibility

        // Save in smaller batches to avoid memory issues
        int batchSize = 100;
        for (int i = 0; i < count; i += batchSize) {
            int currentBatchSize = Math.min(batchSize, count - i);
            List<User> batch = new ArrayList<>(currentBatchSize);

            for (int j = 0; j < currentBatchSize; j++) {
                int userId = i + j;
                User user = buildUser(userId);

                batch.add(user);
            }

            // Save the users first to get IDs
            List<User> savedBatch = userRepository.saveAll(batch);

            // Now assign roles to the saved users
            for (User user : savedBatch) {
                // Assign random roles to this user
                for (int k = 0; k < ROLES_PER_USER; k++) {
                    int roleIndex = random.nextInt(roles.size());
                    Role role = roles.get(roleIndex);

                    // Add role to user
                    user.getRoles().add(new User.RoleReference(role.getId(), role.getName()));

                    // Add user to role
                    role.getUsers().add(new Role.UserReference(user.getId(), user.getUsername()));
                }
            }

            // Update users with their roles
            userRepository.saveAll(savedBatch);

            System.out.println("Created " + (i + currentBatchSize) + " users so far");
        }

        // Update all roles with their users
        roleRepository.saveAll(roles);

        System.out.println("All users and role assignments created.");
    }

    private static User buildUser(int userId) {
        User user = new User();
        user.setUsername("user" + userId);
        user.setPassword("password" + userId);
        user.setEmail("user" + userId + "@example.com");
        user.setRoles(new java.util.HashSet<>());
        return user;
    }
}