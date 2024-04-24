package project.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import project.model.project.Project;

import java.util.Collection;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("Users")
//@Table(name = "Users")
public class AuthUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Indexed(unique = true)
    private String username;
    private String email;
    private String password;
    private boolean active;

    @Enumerated(EnumType.STRING)
    private SystemRole role;


//    @ElementCollection
//    @MapKeyJoinColumn(name = "project_id")
//    @Column(name = "projects")
//    private ArrayList<Project> projects;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
       // return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return username;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isAdmin(){
        return role.equals(SystemRole.SYSTEM_ADMIN);
    }
}
