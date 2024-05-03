package project.model.image;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("ObjectPoints")
@Entity
public class ObjectPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String imageObjectId;
    private String x;
    private String y;
}
