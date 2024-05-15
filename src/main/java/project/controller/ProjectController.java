package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import project.model.project.Project;
import project.model.project.UserProject;
import project.model.project.UserProjectDetails;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.AuthUserService;
import project.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SYSTEM_RESEARCHER')")
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class ProjectController {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
    private static final String PERMISSION_DENIED_MESSAGE = "User doesn't have required authority to provide this operation.";

    private final ProjectService projectService;
    private final AuthUserService userService;

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllProjects() {
        return ResponseEntity.ok(projectService.findAll());
    }

    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    @GetMapping("/getAllByOwnerId/{id}")
    public ResponseEntity<?> getAllByOwnerId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id) {

        AuthUser authorizedUser = getAuthorizedUser(authentication);
        checkUserAuthorization(authorizedUser, id);


        // Retrieve and return the projects owned by the user
        return ResponseEntity.ok(projectService.findProjectsByOwnerId(id));
    }

    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    @GetMapping("/getById/{projectId}")
    public ResponseEntity<?> getById(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String projectId
    ) {
        AuthUser authorizedUser = getAuthorizedUser(authentication);
        checkUserAuthorization(authorizedUser, projectId);

        return ResponseEntity.ok(projectService.findById(projectId));
    }

//    @PutMapping("/update")
//    @PreAuthorize("hasAuthority('admin:update')")
//    public ResponseEntity<?> updateProject(@RequestBody Project project) {
//        return ResponseEntity.ok(projectService.update(project));
//    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('admin:update', 'researcher:update')")
    public ResponseEntity<?> updateProjectBy(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id,
            @RequestBody Project request
    ) {
        AuthUser fetchedUser = getAuthorizedUser(authentication);
        Project fetchedProject = projectService.findById(id);
        checkUserAuthorization(fetchedUser, fetchedProject.getId());

        request.setId(id);
        return ResponseEntity.ok(projectService.update(request));
    }


    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> saveProjectBy(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody Project request) {
        AuthUser authorized = getAuthorizedUser(authentication);
        if (!authorized.isAdmin() && !authorized.getRole().equals(SystemRole.SYSTEM_RESEARCHER)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }
        request.setOwnerId(authorized.getId());
        return ResponseEntity.ok(projectService.save(request));

    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteProject(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) {
        // Retrieve the authorized user and validate existence
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Retrieve the project and validate existence
        Project fetchedProject = projectService.findById(id);
        // Check if the user has edit authority or is SYSTEM_ADMIN
        if (!projectService.userContainsAuthorityToEdit(fetchedProject.getId(), fetchedUser.getId()) &&
                !fetchedUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Perform the delete operation and return value
        return ResponseEntity.ok(projectService.deleteById(id));
    }

    @GetMapping("/users/getByUserIdAndProjectId")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> getUserByUserIdAndProjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestParam("userId") String userId,
            @RequestParam("projectId") String projectId) {
        // Validate and retrieve the authenticated user
        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Validate and retrieve the user by ID
        AuthUser fetchedUser = userService.findAuthUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found for given ID"));

        // Authorization check for non-admin users
        if (!authorized.isAdmin() &&
                !projectService.userContainsAuthorityToEdit(projectId, authorized.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Validate and retrieve the user-project link
        UserProject fetchedLink = projectService.findUserProjectByProjectIdAndUserId(projectId, userId);
        // Construct and return the response
        return ResponseEntity.ok(new UserProjectDetails(fetchedUser, fetchedLink));
    }

    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    @GetMapping("/users/getByProjectId/{projectId}")
    public ResponseEntity<?> getUsersByProjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String projectId
    ) {
        // Retrieve and validate the project
        Project fetchedProject = projectService.findById(projectId);

        // Retrieve and validate the authenticated user
        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));


        // Authorization check for non-admin users
        if (!projectService.userContainsAuthorityToEdit(fetchedProject.getId(), authorized.getId()) &&
                !authorized.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Retrieve and return the user project details
        return ResponseEntity.ok(projectService.getUserProjectDetailsByProjectId(projectId));


    }

    @PostMapping("/users/addUserToProject")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> addUserToProject(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserProject userProject
    ) {
        // Validate and retrieve the authenticated user
        AuthUser authorized = getAuthorizedUser(authentication);

        // Validate the project
        projectService.findById(userProject.getProjectId());
        // Authorization check for non-admin users
        checkUserAuthorization(authorized, userProject.getProjectId());


        // Add user to project
        projectService.addUserToProject(userProject);

        // Retrieve and return the new user project details
        AuthUser user = userService.findAuthUserById(userProject.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
        return ResponseEntity.ok(new UserProjectDetails(user, userProject));
    }

    @DeleteMapping("/users/deleteUserFromProject")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteUserFromProject(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserProject request
    ) {

        // Validate and retrieve the authenticated user
        AuthUser authorized = getAuthorizedUser(authentication);
        // Retrieve and validate the user-project link
        projectService.findUserProjectByProjectIdAndUserId(request.getProjectId(), request.getUserId());

        // Authorization check for non-admin users
        checkUserAuthorization(authorized, request.getProjectId());


        // Validate and retrieve the user to be deleted
        AuthUser deletedUser = userService.findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Delete user from the project and retrieve the link
        UserProject deletedLink = projectService.deleteUserFromProject(request.getProjectId(), request.getUserId());

        // Construct and return the response
        return ResponseEntity.ok(new UserProjectDetails(deletedUser, deletedLink));
    }


    private AuthUser getAuthorizedUser(UserDetails authentication) {
        return userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));
    }

    private void checkUserAuthorization(AuthUser authorizedUser, String projectId) {
        if (!projectService.userContainsAuthorityToEdit(projectId, authorizedUser.getId()) &&
                !authorizedUser.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, PERMISSION_DENIED_MESSAGE);
        }
    }

}
