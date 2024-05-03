package project.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import project.model.dataset.Dataset;
import project.model.dataset.UserDataset;
import project.model.dataset.UserDatasetDetails;
import project.model.project.Project;
import project.model.request.DeleteUserFromDatasetRequest;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SYSTEM_RESEARCHER')")
public class DatasetController {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
    private static final String PROJECT_NOT_FOUND_MESSAGE = "Project not found.";
    private static final String DATASET_NOT_FOUND_MESSAGE = "Dataset not found.";
    private static final String PERMISSION_DENIED_MESSAGE = "User doesn't have required authority to provide this operation.";


    private final DatasetService datasetService;
    private final AuthUserService userService;
    private final ProjectService projectService;

    @GetMapping("/getAll")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<?> getAllDatasets() {
        return ResponseEntity.ok(datasetService.findAll());
    }

    @GetMapping("/getAllByName/{name}")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<?> getAllDatasetsByName(@PathVariable String name) {
        return ResponseEntity.ok(datasetService.findAllByName(name));
    }

    @GetMapping("/getAllByOwnerId/{ownerId}")
    @PreAuthorize("hasAnyAuthority('admin:read','researcher:read')")
    public ResponseEntity<?> getAllByOwnerId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String ownerId
    ) {
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        if (!fetchedUser.getRole().equals(SystemRole.SYSTEM_ADMIN) &&
                !fetchedUser.getId().equals(ownerId))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);

        return ResponseEntity.ok(datasetService.findAllByOwnerId(ownerId));
    }


    @GetMapping("/getAllByProjectId/{projectId}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> getAllDatasetsByProjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String projectId
    ) {
        // Retrieve and validate the authenticated user
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Retrieve and validate the project
        Project fetchedProject = projectService.findById(projectId);

        // Authorization check for non-admin users
        if (!projectService.userContainsAuthorityToEdit(fetchedProject.getId(), fetchedUser.getId()) &&
                !fetchedUser.getRole().equals(SystemRole.SYSTEM_ADMIN))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);


        // Retrieve and return all datasets associated with the project
        return ResponseEntity.ok(datasetService.findAllByProjectId(projectId));
    }

    @GetMapping("/getById/{id}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> getDatasetById(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id) {
        // Retrieve and validate the dataset
//        Dataset dataset = datasetService.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, DATASET_NOT_FOUND_MESSAGE));

        Dataset dataset = datasetService.findById(id);

        // Retrieve and validate the authenticated user
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));


        //Check if user have access to dataset
        datasetService.findUserDatasetByDatasetIdAndUserId(id, fetchedUser.getId());
        // Authorization check for non-admin users
        if (!datasetService.userContainsAuthorityToEdit(id, fetchedUser.getId()) &&
                !fetchedUser.getRole().equals(SystemRole.SYSTEM_ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }
        // Return the dataset
        return ResponseEntity.ok(dataset);
    }


    @PostMapping("/createDataset")
    @PreAuthorize("hasAuthority('admin:create')")
    public ResponseEntity<?> saveDataset(@RequestBody Dataset dataset) {
        return ResponseEntity.ok(datasetService.saveDataset(dataset));
    }

    @PostMapping("/createDatasetBy")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> saveDatasetBy(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody Dataset request
    ) {
        // Retrieve and validate the authenticated user
        AuthUser authorizedUser = userService
                .findAuthUserByUsername(authentication.getUsername())
                .orElseThrow();
        // Retrieve and validate the associated project
        String fetchedProject = projectService.findById(request.getProjectId()).getId();
        // Authorization check for non-admin users
        if (!projectService.userContainsAuthorityToEdit(fetchedProject, authorizedUser.getId()) &&
                !authorizedUser.isAdmin())
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);


        // Set the owner ID and save the dataset
        request.setOwnerId(authorizedUser.getId());
        return ResponseEntity.ok(datasetService.saveDataset(request));
    }


    @PutMapping("/updateDataset")
    @PreAuthorize("hasAuthority('admin:update')")
    public ResponseEntity<?> updateDataset(@RequestBody Dataset dataset) {
        return ResponseEntity.ok(datasetService.updateDataset(dataset));
    }

    @PutMapping("/updateDataset/{id}")
    @PreAuthorize("hasAnyAuthority('admin:update', 'researcher:update')")
    public ResponseEntity<?> updateDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id,
            @RequestBody Dataset request
    ) {
        // Ensure dataset exists
        datasetService.findById(id);
        // Retrieve and validate the authenticated user
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Retrieve and validate the associated project
        Project fetchedProject = projectService.findById(request.getProjectId());
        // Authorization check
        boolean isAuthorized = fetchedUser.getRole().equals(SystemRole.SYSTEM_ADMIN) ||
                datasetService.userContainsAuthorityToEdit(id, fetchedUser.getId()) ||
                projectService.userContainsAuthorityToEdit(fetchedProject.getId(), fetchedUser.getId());

        if (!isAuthorized) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Set the dataset ID to ensure it updates the correct dataset
        request.setId(id);
        Dataset updatedDataset = datasetService.updateDataset(request);
        return ResponseEntity.ok(updatedDataset);
    }


    @DeleteMapping("/deleteDataset/{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) {
        // Ensure the dataset exists before attempting to delete
        Dataset dataset = datasetService.findById(id);
        // Retrieve and validate the authenticated user
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        // Authorization check to see if user has the rights to delete the dataset
        if (!fetchedUser.getRole().equals(SystemRole.SYSTEM_ADMIN) &&
                !datasetService.userContainsAuthorityToEdit(id, fetchedUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        // Perform the delete operation
        ;
        return ResponseEntity.ok(datasetService.deleteDataset(id));
    }


//    @DeleteMapping("/deleteDatasetBy/{id}")
//    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
//    public ResponseEntity<?> deleteDataset(@PathVariable String id, @RequestBody String jwt) {
////        return ResponseEntity.ok(datasetService.deleteDataset(id));
//
//
//        String username = jwtService.extractUserName(jwt);
//        Optional<AuthUser> userOpt = userService.findAuthUsersByUsername(username);
//        if (!userOpt.isPresent()) {
//            return ResponseEntity.badRequest().body("User not found");
//        }
//        AuthUser user = userOpt.get();
//
//        Optional<Dataset> datasetOpt = datasetService.findById(id);
//        if (datasetOpt.isEmpty()) {
//            return ResponseEntity.badRequest().body("Dataset not found");
//        }
//
//        Dataset dataset = datasetOpt.get();
//        if (datasetService.isDatasetOwner(user.getId(), dataset.getOwnerId())) {
//            return ResponseEntity.badRequest().body("User dont have permissions do delete dataset");
//        }
//
//        datasetService.deleteDataset(id);
//        return ResponseEntity.ok(dataset);
//    }

    @DeleteMapping("/deleteAllByProjectId/{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteDatasetsByProjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) {

        Project fetchedProject = projectService.findById(id);
        AuthUser fetchedUser = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        if (!projectService.userContainsAuthorityToEdit(fetchedProject.getId(), fetchedUser.getId()) &&
                !fetchedUser.getRole().equals(SystemRole.SYSTEM_ADMIN))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);

        return ResponseEntity.ok(datasetService.deleteAllByProjectId(id));

    }

    @PostMapping("/users/addUserToDataset")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> addUserToDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserDataset request
    ) {
//        Optional<Dataset> datasetOpt = datasetService.findById(request.getDatasetId());
//        if (datasetOpt.isEmpty()) return ResponseEntity.badRequest().body(DATASET_NOT_FOUND_MESSAGE);

        Dataset dataset = datasetService.findById(request.getDatasetId());

        //Dataset fetchedDataset = datasetOpt.get();

        AuthUser authorized = userService
                .findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));


        AuthUser newUser = userService
                .findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));


        UserDataset authorizedLink = datasetService
                .findUserDatasetByDatasetIdAndUserId(request.getDatasetId(), authorized.getId());

        if (!authorizedLink.getUserRole().equals(SystemRole.PROJECT_OWNER) &&
                !authorizedLink.getUserRole().equals(SystemRole.PROJECT_DATASET) &&
                !authorized.getRole().equals(SystemRole.SYSTEM_ADMIN))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);

        UserDataset newLink = datasetService.addUserToDataset(request);

        return ResponseEntity.ok(
                new UserDatasetDetails(newUser, newLink));

    }


    @DeleteMapping("/deleteUserFromDataset")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteUserFromDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody DeleteUserFromDatasetRequest request
    ) {
        AuthUser authorized = userService
                .findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        AuthUser deletedUser = userService.findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));


        String projectId = datasetService.findById(request.getDatasetId()).getProjectId();
        if (!projectService.userContainsAuthorityToEdit(projectId, authorized.getId()) &&
                !datasetService.userContainsAuthorityToEdit(request.getDatasetId(), authorized.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);
        }

        UserDataset deletedLink = datasetService.deleteUserFromDataset(request.getDatasetId(), request.getUserId());
        return ResponseEntity.ok(new UserDatasetDetails(
                deletedUser, deletedLink
        ));
    }

    @GetMapping("/users/getByDatasetId/{datasetId}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> findUsersByDatasetId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String datasetId) {
        Dataset dataset = datasetService.findById(datasetId);
        Project project = projectService.findById(dataset.getProjectId());

        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        if (!authorized.isAdmin() &&
                !projectService.userContainsAuthorityToEdit(project.getId(), authorized.getId()) &&
                !datasetService.userContainsAuthorityToEdit(datasetId, authorized.getId())
        ) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);


        return ResponseEntity.ok(datasetService.getUserDatasetDetailsByDatasetId(datasetId));

    }

    @GetMapping("/users/getByDatasetIdAndUserId")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> findUserByDatasetIdAndUserId(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserDataset request
    ) {
        // Validate and retrieve the authenticated user
        AuthUser authorized = userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));
        // Validate and retrieve the user by ID
        AuthUser fetchedUser = userService.findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found for given ID"));

        String projectId = datasetService.findById(request.getDatasetId()).getProjectId();
        if (!authorized.isAdmin() &&
                !projectService.userContainsAuthorityToEdit(projectId, authorized.getId()) &&
                !datasetService.userContainsAuthorityToEdit(request.getDatasetId(), authorized.getId())
        ) return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);

        UserDataset fetchedLink = datasetService.findUserDatasetByDatasetIdAndUserId(request.getDatasetId(), request.getUserId());

        return ResponseEntity.ok(new UserDatasetDetails(fetchedUser, fetchedLink));
    }


}