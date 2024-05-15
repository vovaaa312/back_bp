package project.controller;

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
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.*;

@RestController
@RequestMapping("/api/datasets")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SYSTEM_RESEARCHER')")
public class DatasetController {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
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
        AuthUser fetchedUser = getAuthorizedUser(authentication);

        if (!fetchedUser.isAdmin() && !fetchedUser.getId().equals(ownerId))
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
        AuthUser fetchedUser = getAuthorizedUser(authentication);


        // Retrieve and validate the project
        Project fetchedProject = projectService.findById(projectId);

        // Authorization check for non-admin users
//        if (!projectService.userContainsAuthorityToEdit(fetchedProject.getId(), fetchedUser.getId()) &&
//                !fetchedUser.isAdmin())
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);

        checkProjectAuthorization(fetchedUser, projectId);

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


        // Retrieve and validate the authenticated user
        AuthUser fetchedUser = getAuthorizedUser(authentication);

        // Retrieve and validate the dataset
        Dataset dataset = datasetService.findById(id);
        //Check if user have access to dataset
        datasetService.findUserDatasetByDatasetIdAndUserId(id, fetchedUser.getId());
        // Authorization check for non-admin users
        checkDatasetAuthorization(fetchedUser, id);
        // Return the dataset
        return ResponseEntity.ok(dataset);
    }




    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> saveDatasetBy(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody Dataset request
    ) {
        // Retrieve and validate the authenticated user
        AuthUser authorizedUser = getAuthorizedUser(authentication);
        // Retrieve and validate the associated project
        String fetchedProject = projectService.findById(request.getProjectId()).getId();
        // Authorization check for non-admin users
        checkProjectAuthorization(authorizedUser, fetchedProject);


        // Set the owner ID and save the dataset
        request.setOwnerId(authorizedUser.getId());
        return ResponseEntity.ok(datasetService.saveDataset(request));
    }


    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('admin:update', 'researcher:update')")
    public ResponseEntity<?> updateDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id,
            @RequestBody Dataset request
    ) {

        // Retrieve and validate the authenticated user
        AuthUser fetchedUser = getAuthorizedUser(authentication);

        // Ensure dataset exists
        datasetService.findById(id);
        // Retrieve and validate the associated project
        String fetchedProject = projectService.findById(request.getProjectId()).getId();
        // Authorization check
        checkDatasetAuthorization(fetchedUser, id);
        checkProjectAuthorization(fetchedUser, fetchedProject);

        // Set the dataset ID to ensure it updates the correct dataset
        request.setId(id);
        return ResponseEntity.ok(datasetService.updateDataset(request));
    }


    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) {

        // Retrieve and validate the authenticated user
        AuthUser fetchedUser = getAuthorizedUser(authentication);

        // Ensure the dataset exists before attempting to delete
        Dataset dataset = datasetService.findById(id);
        // Authorization check to see if user has the rights to delete the dataset
        checkDatasetAuthorization(fetchedUser, id);

        // Perform the delete operation
        ;
        return ResponseEntity.ok(datasetService.deleteDataset(id));
    }


    @DeleteMapping("/deleteAllByProjectId/{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteDatasetsByProjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) {

        AuthUser fetchedUser = getAuthorizedUser(authentication);
        Project fetchedProject = projectService.findById(id);

        checkProjectAuthorization(fetchedUser, fetchedProject.getId());

        return ResponseEntity.ok(datasetService.deleteAllByProjectId(id));

    }

    @PostMapping("/users/addUserToDataset")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> addUserToDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserDataset request
    ) {

        AuthUser authorized = getAuthorizedUser(authentication);
        datasetService.findById(request.getDatasetId());

        AuthUser newUser = userService
                .findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));

        checkProjectAuthorization(authorized, request.getDatasetId());
        UserDataset newLink = datasetService.addUserToDataset(request);
        return ResponseEntity.ok(new UserDatasetDetails(newUser, newLink));

    }


    @DeleteMapping("/users/deleteUserFromDataset")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteUserFromDataset(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserDataset request
    ) {
        AuthUser authorized = getAuthorizedUser(authentication);

        AuthUser deletedUser = userService.findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));


        String projectId = datasetService.findById(request.getDatasetId()).getProjectId();

        checkProjectAuthorization(authorized, projectId);
        checkDatasetAuthorization(authorized, request.getDatasetId());

        UserDataset deletedLink = datasetService.deleteUserFromDataset(request.getDatasetId(), request.getUserId());
        return ResponseEntity.ok(new UserDatasetDetails(deletedUser, deletedLink));
    }

    @GetMapping("/users/getByDatasetId/{datasetId}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> findUsersByDatasetId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String datasetId) {
        Dataset dataset = datasetService.findById(datasetId);
        Project project = projectService.findById(dataset.getProjectId());

        AuthUser authorized = getAuthorizedUser(authentication);

        checkProjectAuthorization(authorized, project.getId());
        checkDatasetAuthorization(authorized, datasetId);

        return ResponseEntity.ok(datasetService.getUserDatasetDetailsByDatasetId(datasetId));

    }

    @GetMapping("/users/getByDatasetIdAndUserId")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> findUserByDatasetIdAndUserId(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody UserDataset request
    ) {
        // Validate and retrieve the authenticated user
        AuthUser authorized = getAuthorizedUser(authentication);
        // Validate and retrieve the user by ID
        AuthUser fetchedUser = userService.findAuthUserById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found for given ID."));

        String projectId = datasetService.findById(request.getDatasetId()).getProjectId();
        String datasetId = request.getDatasetId();

        checkProjectAuthorization(authorized, projectId);
        checkDatasetAuthorization(authorized, datasetId);

        UserDataset fetchedLink = datasetService.findUserDatasetByDatasetIdAndUserId(request.getDatasetId(), request.getUserId());

        return ResponseEntity.ok(new UserDatasetDetails(fetchedUser, fetchedLink));
    }


    private AuthUser getAuthorizedUser(UserDetails authentication) {
        return userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));
    }

    private void checkProjectAuthorization(AuthUser authorizedUser, String projectId) {
        if (projectService.userContainsAuthorityToEdit(projectId, authorizedUser.getId()) || authorizedUser.isAdmin())
            return;

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, PERMISSION_DENIED_MESSAGE);
    }

    private void checkDatasetAuthorization(AuthUser authorizedUser, String datasetId) {

        if (datasetService.userContainsAuthorityToEdit(datasetId, authorizedUser.getId()) || authorizedUser.isAdmin())
            return;


        throw new ResponseStatusException(HttpStatus.FORBIDDEN, PERMISSION_DENIED_MESSAGE);

    }
}