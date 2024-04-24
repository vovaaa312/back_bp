package project.model.dataset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.model.user.AuthUser;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDatasetDetails {
    AuthUser user;
    UserDataset userDataset;
}
