package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import project.model.project.Project;
import project.model.project.UserProject;
import project.model.project.UserProjectDetails;
import project.model.request.DeleteUserProjectRequest;
import project.model.response.UsersProjectResponse;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.AuthUserService;
import project.service.ProjectService;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SYSTEM_RESEARCHER')")
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class ProjectController {
    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
    private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found.";
    private static final String PERMISSION_DENIED_MESSAGE = "User doesn't have required authority to provide this operation.";
    private static final String OUT_OF_PROJECT_MESSAGE = "The user is not in the list of project users.";

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

        // Retrieve the authorized user and validate existence
        AuthUser authorizedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Check if the requested ID matches the authorized user's ID or if the user is a SYSTEM_ADMIN
//        if (!authorizedUser.getId().equals(id) &&
//                !authorizedUser.getRole().equals(SystemRole.SYSTEM_ADMIN)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
//        }

        if (!authorizedUser.getId().equals(id) &&
                !authorizedUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Retrieve and return the projects owned by the user
        return ResponseEntity.ok(projectService.findProjectsByOwnerId(id));
    }

    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    @GetMapping("/getById/{projectId}")
    public ResponseEntity<?> getById(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String projectId
    ) {
        // Retrieve the authorized user and validate existence
        AuthUser authorizedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Check project association and existence
        projectService.findUserProjectByProjectIdAndUserId(projectId, authorizedUser.getId());
        // Check if the user has edit authority or is SYSTEM_ADMIN
        if (!projectService.userContainsAuthorityToEdit(projectId, authorizedUser.getId()) &&
                !authorizedUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Retrieve and return the project by its ID
        return ResponseEntity.ok(projectService.findById(projectId));
    }

    @PutMapping("/updateProject")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<?> updateProject(@RequestBody Project project) {
        return ResponseEntity.ok(projectService.update(project));
    }

    @PutMapping("/updateProject/{id}")
    @PreAuthorize("hasAnyAuthority('admin:update', 'researcher:update')")
    public ResponseEntity<?> updateProjectBy(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id,
            @RequestBody Project request
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

        // Set the ID to ensure the correct project is updated and perform the update
        request.setId(id);
        return ResponseEntity.ok(projectService.update(request));
    }

    @PostMapping("/createProject")
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<?> saveProject(@RequestBody Project project) {
        return ResponseEntity.ok(projectService.save(project));

    }

    @PostMapping("/createProjectBy")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> saveProjectBy(@AuthenticationPrincipal UserDetails authentication, @RequestBody Project request) {
        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername()).get();
        request.setOwnerId(authorized.getId());
        return ResponseEntity.ok(projectService.save(request));

    }

    @DeleteMapping("/deleteProject/{projectId}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteProject(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String projectId
    ) {
        // Retrieve the authorized user and validate existence
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Retrieve the project and validate existence
        Project fetchedProject = projectService.findById(projectId);
        // Check if the user has edit authority or is SYSTEM_ADMIN
        if (!projectService.userContainsAuthorityToEdit(fetchedProject.getId(), fetchedUser.getId()) &&
                !fetchedUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Perform the delete operation and return value
        return ResponseEntity.ok(projectService.deleteById(projectId));
    }

//    @GetMapping("/users/getByUserIdAndProjectId/prjId={projectId}/usrId={userId}")
//    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
//    public ResponseEntity<?> getUserByUserIdAndProjectId(
//            @AuthenticationPrincipal UserDetails authentication,
//            @PathVariable String projectId,
//            @PathVariable String userId
//    ) {
//        // Validate and retrieve the authenticated user
//        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));
//
//        // Validate and retrieve the user by ID
//        AuthUser fetchedUser = userService.findAuthUserById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found for given ID"));
//
//        // Validate and retrieve the user-project link
//        UserProject fetchedLink = projectService.findUserProjectByProjectIdAndUserId(projectId, userId);
//        // Authorization check for non-admin users
//        if (!authorized.isAdmin() &&
//                !projectService.userContainsAuthorityToEdit(projectId, authorized.getId())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
//        }
//
//        // Construct and return the response
//        return ResponseEntity.ok(new UsersProjectResponse(fetchedUser, fetchedLink));
//    }

    @GetMapping("/users/getByUserIdAndProjectId")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> getUserByUserIdAndProjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserProject request
    ) {
        // Validate and retrieve the authenticated user
        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Validate and retrieve the user by ID
        AuthUser fetchedUser = userService.findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found for given ID"));

      // Authorization check for non-admin users
        if (!authorized.isAdmin() &&
                !projectService.userContainsAuthorityToEdit(request.getProjectId(), authorized.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Validate and retrieve the user-project link
        UserProject fetchedLink = projectService.findUserProjectByProjectIdAndUserId(request.getProjectId(), request.getUserId());
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
        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Validate the project
        projectService.findById(userProject.getProjectId());
        // Authorization check for non-admin users
        if (!projectService.userContainsAuthorityToEdit(userProject.getProjectId(), authorized.getId()) &&
                !authorized.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

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
        // Retrieve and validate the user-project link
        projectService.findUserProjectByProjectIdAndUserId(request.getProjectId(), request.getUserId());
        // Validate and retrieve the authenticated user
        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Authorization check for non-admin users
        if (!authorized.isAdmin() &&
                !projectService.userContainsAuthorityToEdit(request.getProjectId(), authorized.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Validate and retrieve the user to be deleted
        AuthUser deletedUser = userService.findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Delete user from the project and retrieve the link
        UserProject deletedLink = projectService.deleteUserFromProject(request.getProjectId(), request.getUserId());

        // Construct and return the response
        return ResponseEntity.ok(new UserProjectDetails(deletedUser, deletedLink));
    }


}
