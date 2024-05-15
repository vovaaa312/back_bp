package project.model.dataset;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import project.model.user.SystemRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("UserDataset")
@Entity
public class UserDataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String userId;
    private String datasetId;
    private SystemRole userRole;
}
