package project.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.model.project.UserProject;
import project.model.user.AuthUser;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsersProjectResponse {
    AuthUser user;
    UserProject userProject;
}
