package project.model.user;

import lombok.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

import static project.model.user.SystemPermission.*;

@RequiredArgsConstructor
public enum SystemRole {


    //system level roles
    SYSTEM_USER(Set.of(USER_READ)),
    SYSTEM_ADMIN(getAllPermissions()),
    SYSTEM_RESEARCHER(getResearcherPermissions()),

    //project level roles
    PROJECT_OWNER(getProjectOwnerPermissions()),
    PROJECT_DATASET(getProjectDatasetPermissions()),

    //dataset level roles
    DATASET_OWNER(getDatasetOwnerPermissions()),
    DATASET_LABEL(getDatasetLabelPermissions()),
    DATASET_VIEWER(getDatasetViewerPermissions());

    @Getter
    private final Set<SystemPermission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        var authorities = getPermissions()
                .stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority(this.name()));
        return authorities;
    }
}
