package project.model.project;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.core.mapping.Document;
import project.model.user.AuthUser;
import project.model.user.SystemRole;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("Projects")
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;
    private String ownerId;
    private Date creationTimestamp;

//    @ElementCollection
//    @MapKeyJoinColumn(name = "user_id")
//    @Column(name = "role")
//    private Map<AuthUser, SystemRole> userRoles = new HashMap<>();


}
