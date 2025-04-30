package kaixin.learning.mongodb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String password;

    private String email;

    private Set<RoleReference> roles = new HashSet<>();

    private Set<ObjectId> refRoles = new HashSet<>();

    private List<Role> completeRoles = new ArrayList<>();

    // Embedded reference to roles
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleReference {
        private String roleId;
        private String name;
    }
}