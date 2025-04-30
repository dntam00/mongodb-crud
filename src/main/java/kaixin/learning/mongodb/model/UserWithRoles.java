package kaixin.learning.mongodb.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserWithRoles {

    private String id;
    private String username;
    private List<Role> roles;  // Fetched role details
}
