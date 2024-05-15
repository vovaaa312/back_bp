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

    P_CREATE("project:create"),
    P_READ("project:read"),
    P_UPDATE("project:update"),
    P_DELETE("project:delete"),

    P_CREATE_DATASET("project:create-dataset"),
    P_READ_DATASET("project:read-dataset"),
    P_UPDATE_DATASET("project:update-dataset"),
    P_DELETE_DATASET("project:delete-dataset"),

    P_ADD_USER("project:add-user"),
    P_DELETE_USER("project:delete-user"),
    P_UPDATE_ROLE("project:update-role"),

    D_CREATE("dataset:create"),
    D_READ("dataset:read"),
    D_UPDATE("dataset:update"),
    D_DELETE("dataset:delete"),

    D_CREATE_IMAGE("project:create-image"),
    D_READ_IMAGE("project:read-image"),
    D_UPDATE_IMAGE("project:update-image"),
    D_DELETE_IMAGE("project:delete-image"),

    D_ADD_USER("dataset:add-user"),
    D_DELETE_USER("dataset:delete-user"),
    D_UPDATE_ROLE("dataset:update-role");


    @Getter
    private final String permission;

    public static Set<SystemPermission> getAllPermissions() {
        return Set.of(SystemPermission.values());
    }

    public static Set<SystemPermission> getResearcherPermissions() {
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(RESEARCHER_CREATE);
        permissions.add(RESEARCHER_READ);
        permissions.add(RESEARCHER_UPDATE);
        permissions.add(RESEARCHER_DELETE);

        return permissions;
    }

    public static Set<SystemPermission> getProjectOwnerPermissions() {
        Set<SystemPermission> permissions = new HashSet<>();
        permissions.add(P_CREATE);
        permissions.add(P_READ);
        permissions.add(P_UPDATE);
        permissions.add(P_DELETE);

        permissions.add(P_ADD_USER);
        permissions.add(P_DELETE_USER);
        permissions.add(P_UPDATE_ROLE);

        return permissions;
    }

    public static Set<SystemPermission> getProjectDatasetPermissions() {
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(P_CREATE);
        permissions.add(P_READ);

        permissions.add(P_ADD_USER);
        permissions.add(P_DELETE_USER);
        permissions.add(P_UPDATE_ROLE);
        return permissions;
    }

    public static Set<SystemPermission> getDatasetOwnerPermissions() {
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(D_CREATE);
        permissions.add(D_READ);
        permissions.add(D_UPDATE);
        permissions.add(D_DELETE);

        permissions.add(D_CREATE_IMAGE);
        permissions.add(D_READ_IMAGE);
        permissions.add(D_UPDATE_IMAGE);
        permissions.add(D_DELETE_IMAGE);

        permissions.add(D_ADD_USER);
        permissions.add(D_DELETE_USER);
        permissions.add(D_UPDATE_ROLE);

        return permissions;
    }


    public static Set<SystemPermission> getDatasetLabelPermissions() {
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(D_CREATE);
        permissions.add(D_READ);

        permissions.add(D_CREATE_IMAGE);
        permissions.add(D_READ_IMAGE);
        permissions.add(D_UPDATE_IMAGE);

        permissions.add(D_ADD_USER);
        permissions.add(D_DELETE_USER);
        permissions.add(D_UPDATE_ROLE);

        return permissions;
    }

    public static Set<SystemPermission> getDatasetViewerPermissions() {
        Set<SystemPermission> permissions = new HashSet<>();

        permissions.add(D_CREATE);
        permissions.add(D_READ);

        permissions.add(D_READ_IMAGE);

        return permissions;
    }

}
