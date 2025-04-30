package kaixin.learning.mongodb.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private Set<UserReference> users = new HashSet<>();

    // Embedded reference to users
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserReference {
        private String userId;
        private String username;
    }
}