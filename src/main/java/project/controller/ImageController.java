package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import project.model.dataset.UserDataset;
import project.model.image.Image;
import project.model.user.SystemRole;
import project.service.AuthUserService;
import project.service.DatasetService;
import project.service.ImageService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SYSTEM_RESEARCHER')")

public class ImageController {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found.";
    private static final String DATASET_NOT_FOUND_MESSAGE = "Dataset not found.";
    private static final String PERMISSION_DENIED_MESSAGE = "User doesn't have required authority to provide this operation.";


    private final ImageService imageService;
    private final DatasetService datasetService;
    private final AuthUserService userService;

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getAll")
    public ResponseEntity<List<Image>> getAllImages() {
        return ResponseEntity.ok(imageService.findAll());
    }

    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    @GetMapping("/getAllByName/{name}")
    public ResponseEntity<List<Image>> getAllByName(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String name) {

        String authorized = userService
                .findAuthUserByUsername(authentication.getUsername())
                .get()
                .getId();


        return ResponseEntity.ok(imageService.findImagesByName(name).get());
    }

//    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
//    @PostMapping("/saveImage")
//    public ResponseEntity<?> saveImage(
//            @AuthenticationPrincipal UserDetails authentication,
//            @RequestBody Image image
//    ){
//        return ResponseEntity.ok(imageService.saveImage(image));
//    }

    @PostMapping("/saveImage")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> saveImage(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestParam("name") String name,
            @RequestParam("format") String format,
            @RequestParam("data") MultipartFile data,
            @RequestParam("datasetId") String datasetId
    ) throws IOException {
        // Convert MultipartFile to your Image class and then save
        // This is just a placeholder; actual conversion code will depend on your needs

        String authorized = userService
                .findAuthUserByUsername(authentication.getUsername())
                .get()
                .getId();

        UserDataset userDataset = datasetService.findUserDatasetByDatasetIdAndUserId(datasetId, authorized);
//       userDataset.getUserRole().g
        if (!userDataset.getUserRole().equals(SystemRole.DATASET_OWNER)
        && !userDataset.getUserRole().equals(SystemRole.DATASET_LABEL)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(PERMISSION_DENIED_MESSAGE);

        }
        Image image = new Image(null, name, format, data.getBytes(), datasetId);
        return ResponseEntity.ok(imageService.saveImage(image));
    }


}
