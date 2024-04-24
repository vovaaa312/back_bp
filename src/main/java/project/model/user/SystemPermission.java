package project.model.user;

import lombok.Generated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public enum SystemPermission {
    ADMIN_CREATE("admin:create"),
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),
    USER_CREATE("user:create"),
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_DELETE("user:delete"),

    RESEARCHER_CREATE("researcher:create"),
    RESEARCHER_READ("researcher:read"),
    RESEARCHER_UPDATE("researcher:update"),
    RESEARCHER_DELETE("researcher:delete"),

    P_READ("project:owner"),
    P_UPDATE("project:update"),
    P_DELETE("project:dataset"),

    P_ADD_USER("project:add-user"),
    P_DELETE_USER("project:delete-user"),

    P_UPDATE_ROLE("project:update-role"),

    P_CREATE_DATASET("project:create-dataset"),
    P_DELETE_DATASET("project:delete-dataset"),
    P_UPDATE_DATASET("project:update-dataset"),
    P_COPY_DATASET("project:copy-dataset");


    @Getter
    private final String permission;
public static Set<SystemPermission> getAllPermissions(){
    return Set.of(SystemPermission.values());
}
public static Set<SystemPermission>getResearcherPermissions(){
    Set<SystemPermission> permissions = new HashSet<>();

    permissions.add(RESEARCHER_CREATE);
    permissions.add(RESEARCHER_READ);
    permissions.add(RESEARCHER_UPDATE);
    permissions.add(RESEARCHER_DELETE);

    return permissions;
}

    public static Set<SystemPermission>getProjectOwnerPermissions(){
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(P_READ);
        permissions.add(P_UPDATE);
        permissions.add(P_ADD_USER);
        permissions.add(P_DELETE_USER);
        permissions.add(P_UPDATE_ROLE);

        return permissions;
    }

    public static Set<SystemPermission>getProjectDatasetPermissions(){
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(P_READ);
        permissions.add(P_UPDATE);
//        permissions.add(P_ADD_USER);
//        permissions.add(P_DELETE_USER);
//        permissions.add(P_UPDATE_ROLE);

        return permissions;
    }

}
