package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import project.model.image.Image;
import project.model.image.ImageObject;
import project.model.image.ObjectPoint;
import project.model.user.AuthUser;
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
    private static final String PERMISSION_DENIED_MESSAGE = "User doesn't have required authority to provide this operation.";


    private final ImageService imageService;
    private final DatasetService datasetService;
    private final AuthUserService userService;
//    private final GridFsTemplate gridFsTemplate;

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getAll")
    public ResponseEntity<List<Image>> getAllImages() {
        return ResponseEntity.ok(imageService.findAllImages());
    }

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/findAllByCatagoryContains")
    public ResponseEntity<List<Image>> findAllByCatagoryContains(
            @RequestParam("category") String category
    ) {
        return ResponseEntity.ok(imageService.findAllImagesByCategory(category));
    }

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/findAllByCatagories")
    public ResponseEntity<List<Image>> findAllByCatagories(
            @RequestParam("categories") List<String> categories

    ) {
        return ResponseEntity.ok(imageService.findAllImagesByCategoriesContainingAll(categories));
    }

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/findAllByCatagoriesAndDatasetId")
    public ResponseEntity<?> findAllByCatagories(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestParam("datasetId") String datasetId,
            @RequestParam("categories") List<String> categories

    ) {
        AuthUser authorized = getAuthorizedUser(authentication);
        checkDatasetAuthorization(authorized, datasetId);


        return ResponseEntity.ok(imageService.findAllImagesByCategoriesContainingAllAndDatasetId(categories, datasetId));
    }


    @PostMapping("/save")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> saveImage(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestParam("name") String name,
            @RequestParam("data") MultipartFile data,
            @RequestParam("datasetId") String datasetId,
            @RequestParam("categories") List<String> categories
    ) throws IOException {

        AuthUser authorized = getAuthorizedUser(authentication);
        checkDatasetAuthorization(authorized, datasetId);

        Image image = new Image(null, name, data.getContentType(), data.getBytes(), datasetId, categories);
        return ResponseEntity.ok(imageService.saveImage(image, data));
    }


    @GetMapping("/getAllByDatasetId/{datasetId}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> getAllImagesByDatasetId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String datasetId    ) {
        AuthUser authorized = getAuthorizedUser(authentication);

        checkDatasetAuthorization(authorized, datasetId);

        return ResponseEntity.ok(imageService.findAllImagesByDatasetId(datasetId));
    }

    @GetMapping("/getById/{imageId}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> getById(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String imageId    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        Image existed = imageService.findImageById(imageId);

        checkDatasetAuthorization(authorized, existed.getDatasetId());

        return ResponseEntity.ok(existed);
    }

    @DeleteMapping("/deleteById/{imageId}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteById(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String imageId    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        Image existed = imageService.findImageById(imageId);

        checkDatasetAuthorization(authorized, existed.getDatasetId());

        return ResponseEntity.ok(imageService.deleteImage(imageId));
    }

    @DeleteMapping("/deleteAllByDatasetId/{datasetId}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteAllByDatasetId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String datasetId    ) {
        AuthUser authorized = getAuthorizedUser(authentication);
        checkDatasetAuthorization(authorized, datasetId);

        return ResponseEntity.ok(imageService.deleteAllImagesByDatasetId(datasetId));
    }


    @GetMapping("/objects/findAll")
    @PreAuthorize("hasAuthority('admin:read')")
    public ResponseEntity<?> findAllObjects() {
        return ResponseEntity.ok(imageService.findAllImageObjects());
    }

    @GetMapping("/objects/findAllByImageId/{imageId}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> findAllObjectsByImageId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String imageId
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        String datasetId = imageService.findImageById(imageId).getDatasetId();
        checkDatasetAuthorization(authorized, datasetId);

        return ResponseEntity.ok(imageService.findAllImageObjectsByImageId(imageId));
    }

    @PostMapping("/objects/save")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> saveImageObject(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody ImageObject imageObject
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        String datasetId = imageService.findImageById(imageObject.getImageId()).getDatasetId();
        checkDatasetAuthorization(authorized, datasetId);

        return ResponseEntity.ok(imageService.saveImageObject(imageObject));
    }

    @PutMapping("/objects/update/{id}")
    @PreAuthorize("hasAnyAuthority('admin:update', 'researcher:update')")
    public ResponseEntity<?> updateImageObject(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id,
            @RequestBody ImageObject imageObject
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        String datasetId = imageService.findImageById(imageObject.getImageId()).getDatasetId();

        checkDatasetAuthorization(authorized, datasetId);

        imageObject.setId(id);
        return ResponseEntity.ok(imageService.updateImageObject(imageObject));
    }

    @DeleteMapping("/objects/deleteById/{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteImageObjectById(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        String imageId = imageService.findImageObjectById(id).getImageId();
        String datasetId = imageService.findImageById(imageId).getDatasetId();
        checkDatasetAuthorization(authorized, datasetId);
        return ResponseEntity.ok(imageService.deleteImageObject(id));
    }

    @DeleteMapping("/objects/deleteAllByImageId/{id}")
    @PreAuthorize("hasAnyAuthority('admin:delete', 'researcher:delete')")
    public ResponseEntity<?> deleteImageObjectsByImageId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        String datasetId = imageService.findImageById(id).getDatasetId();
        checkDatasetAuthorization(authorized, datasetId);
        return ResponseEntity.ok(imageService.deleteAllObjectByImageId(id));
    }

    @GetMapping("/objects/points/getAll")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public ResponseEntity<?> getAllPoints() {
        return ResponseEntity.ok(imageService.findAllObjectPoints());
    }

    @GetMapping("/objects/points/getAllByObjectId/{id}")
    @PreAuthorize("hasAnyAuthority('admin:read', 'researcher:read')")
    public ResponseEntity<?> getAllPointsByObjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        //fetch image by object.getImageId then fetch datasetId by image.getDatasetId
        String imageId = imageService.findImageObjectById(id).getImageId();
        String datasetId = imageService.findImageById(imageId).getDatasetId();
        checkDatasetAuthorization(authorized, datasetId);
        return ResponseEntity.ok(imageService.findAllObjectPointsByImageObjectId(id));
    }

    @PostMapping("/objects/points/save")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> saveObjectPoint(
            @AuthenticationPrincipal UserDetails authentication,
            @RequestBody ObjectPoint objectPoint
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);

        //fetch image by object.getImageId then fetch datasetId by image.getDatasetId
        String imageId = imageService.findImageObjectById(objectPoint.getImageObjectId()).getImageId();
        String datasetId = imageService.findImageById(imageId).getDatasetId();

        checkDatasetAuthorization(authorized, datasetId);

        return ResponseEntity.ok(imageService.saveObjectPoint(objectPoint));
    }


    @DeleteMapping("/objects/points/delete/{id}")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> deletePoint(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);

        //fetch image by object.getImageId then fetch datasetId by image.getDatasetId
        ObjectPoint fetched = imageService.findObjectPointById(id);
        String imageId = imageService.findImageObjectById(fetched.getImageObjectId()).getImageId();
        String datasetId = imageService.findImageById(imageId).getDatasetId();
        checkDatasetAuthorization(authorized, datasetId);
        return ResponseEntity.ok(imageService.deleteObjectPoint(id));
    }


    @DeleteMapping("/objects/points/deleteAllByObjcetId/{id}")
    @PreAuthorize("hasAnyAuthority('admin:create', 'researcher:create')")
    public ResponseEntity<?> deleteAllPointByObjectId(
            @AuthenticationPrincipal UserDetails authentication,
            @PathVariable String id
    ) throws IOException {
        AuthUser authorized = getAuthorizedUser(authentication);
        ImageObject object = imageService.findImageObjectById(id);
        String imageId = imageService.findImageById(object.getImageId()).getId();
        String datasetId = imageService.findImageById(imageId).getDatasetId();
        checkDatasetAuthorization(authorized, datasetId);
        return ResponseEntity.ok(imageService.deleteAllObjectPointByObjectId(id));
    }

    private AuthUser getAuthorizedUser(UserDetails authentication) {
        return userService.findAuthUserByUsername(authentication.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, USER_NOT_FOUND_MESSAGE));
    }
    private void checkDatasetAuthorization(AuthUser authorizedUser, String datasetId) {

        if (datasetService.userContainsAuthorityToEdit(datasetId, authorizedUser.getId()) || authorizedUser.isAdmin())
            return;


        throw new ResponseStatusException(HttpStatus.FORBIDDEN, PERMISSION_DENIED_MESSAGE);

    }


}
