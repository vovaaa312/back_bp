package project.model.project;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import project.model.user.AuthUser;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data

public class UserProjectDetails {
    AuthUser user;
    UserProject userProject;
}
