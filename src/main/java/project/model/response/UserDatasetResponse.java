package project.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.model.dataset.UserDataset;
import project.model.user.AuthUser;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDatasetResponse {
    AuthUser user;
    UserDataset userDataset;
}
