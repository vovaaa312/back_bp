package project.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.commons.io.IOUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import project.model.exception.DatasetNotFoundException;
import project.model.exception.ImageNotFoundException;
import project.model.image.Image;
import project.model.image.ImageObject;
import project.model.image.ObjectPoint;
import project.service.repository.*;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageObjectService imageObjectService;
    private final ObjectPointRepository objectPointRepository;
    private final DatasetRepository datasetRepository;

    private final GridFsTemplate gridFsTemplate;
    private final AuthUserRepository userRepository;

    private final GridFsOperations operations;


    public List<Image> findAllImages() {
        return imageRepository.findAll();
    }

    public List<Image> findAllImagesByCategory(String category) {
        return imageRepository.findByCategoryContains(category).orElse(Collections.emptyList());
    }

    public List<Image> findAllImagesByCategoriesContainingAllAndDatasetId(List<String> categories, String datasetId) {
        return imageRepository.findByCategoriesContainingAllAndDatasetId(categories, datasetId)
                .orElse(Collections.emptyList());
    }

    public List<Image> findAllImagesByCategoriesContainingAll(List<String> categories) {
        return imageRepository.findByCategoriesContainingAll(categories).orElse(Collections.emptyList());
    }

    public List<Image> findImagesByName(String name) {
        return imageRepository.findAllByName(name).orElse(Collections.emptyList());
    }

    public List<Image> findAllImagesByDatasetId(String datasetId) {
        return imageRepository.findAllByDatasetId(datasetId).orElse(Collections.emptyList());
    }

    public Image findImageById(String id) throws IOException {
        Image find = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria.where("metadata.imageId").is(id)));
//        assert gridFSFile != null;
        if(gridFSFile != null)find.setData(IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()));
        return find;
    }

    public Image saveImage(Image image, MultipartFile data) throws IOException {
       Image save = imageRepository.save(image);

        DBObject metadata = new BasicDBObject();
        metadata.put("imageId", image.getId());

        gridFsTemplate.store(data.getInputStream(), data.getOriginalFilename(), data.getContentType(), metadata);
        return save;
    }



    public Image deleteImage(String id) {
        Image deleted = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));

        //TODO: add throw UnsupportedOperationException if points list not empty
        if (!imageObjectService.findAllByImageId(id).isEmpty()) {
            throw new UnsupportedOperationException("Objects list of this image is not empty.");
        }

        gridFsTemplate.delete(new Query(Criteria.where("metadata.imageId").is(id)));
        imageRepository.delete(deleted);
        return deleted;
    }

    public List<Image> deleteAllImagesByDatasetId(String datasetId) {
        List<Image> deletedImages = new ArrayList<>();
        List<Image> images = imageRepository.findAllByDatasetId(datasetId).orElse(Collections.emptyList());

        images.forEach(image -> {
            if (imageObjectService.findAllByImageId(image.getId()).isEmpty()) {
                deletedImages.add(image);
                imageRepository.delete(image);
                imageObjectService.deleteAllByImageId(image.getId());

            }
        });
        return deletedImages;
    }


    public List<ImageObject> findAllImageObjects() {
        return imageObjectService.findAll();
    }

    public List<ImageObject> findAllImageObjectsByImageId(String id) {
        return imageObjectService.findAllByImageId(id);
    }

    public ImageObject findImageObjectById(String id) {
        return imageObjectService.findById(id);
    }

    public ImageObject saveImageObject(ImageObject imageObject) {
//        findById(imageObject.getImageId());
        return imageObjectService.save(imageObject);
    }

    public ImageObject updateImageObject(ImageObject imageObject) {
        imageRepository.findById(imageObject.getImageId())
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));
        ImageObject existingObject = imageObjectService.findById(imageObject.getId());
        existingObject.setName(imageObject.getName());
        existingObject.setImageId(imageObject.getImageId());
        return imageObjectService.save(existingObject);
    }

    public ImageObject deleteImageObject(String id) {
        ImageObject deleted = imageObjectService.findById(id);//        if (!objectPointRepository.findAllByObjectId(id).isEmpty()) {
//            throw new UnsupportedOperationException("Points list of this object is not empty.");
//        }
        objectPointRepository.deleteAllByImageObjectId(id);
        imageObjectService.deleteById(id);
        return deleted;
    }

    public List<ImageObject> deleteAllObjectByImageId(String id) {
        List<ImageObject> objects = imageObjectService.deleteAllByImageId(id);

        objects.forEach(object -> objectPointRepository.deleteAllByImageObjectId(object.getId()));


        return objects;
    }

    public List<ObjectPoint> findAllObjectPoints() {
        return objectPointRepository.findAll();
    }

    public List<ObjectPoint> findAllObjectPointsByImageObjectId(String objectId) {
        return objectPointRepository.findAllByImageObjectId(objectId).orElse(Collections.emptyList());
    }

    public ObjectPoint findObjectPointById(String id) {
        return objectPointRepository.findById(id).orElseThrow();
    }

    public ObjectPoint saveObjectPoint(ObjectPoint objectPoint) {
        return objectPointRepository.save(objectPoint);
    }


    public ObjectPoint deleteObjectPoint(String id) {
        ObjectPoint deleted = findObjectPointById(id);
        objectPointRepository.deleteById(id);
        return deleted;
    }

    public List<ObjectPoint> deleteAllObjectPointByObjectId(String id) {
        return objectPointRepository.deleteAllByImageObjectId(id).orElse(Collections.emptyList());
    }

    public List<Image> clearDataset(String datasetId) {
        datasetRepository.findById(datasetId)
                .orElseThrow(() -> new DatasetNotFoundException("Dataset not found.")

                );
        List<Image> datasetImages = findAllImagesByDatasetId(datasetId);

        datasetImages.forEach(image -> {
            // imageObject
            imageObjectService.deleteAllByImageId(image.getId());
        });
        return deleteAllImagesByDatasetId(datasetId);
    }
}
