package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.model.exception.DatasetNotFoundException;
import project.model.exception.ImageNotFoundException;
import project.model.image.Image;
import project.model.image.ImageObject;
import project.model.image.ObjectPoint;
import project.service.repository.DatasetRepository;
import project.service.repository.ImageRepository;
import project.service.repository.ObjectPointRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageObjectService imageObjectService;
    private final ObjectPointRepository objectPointRepository;
    private final DatasetRepository datasetRepository;

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

    public Image findImageById(String id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));
    }

    public Image saveImage(Image image) {
        //datasetService.findById(image.getDatasetId());
        return imageRepository.save(image);
    }

    public Image updateImage(Image image) {
        Image existingImage = imageRepository.findById(image.getId())
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));
        //datasetService.findById(image.getDatasetId());
        existingImage.setName(image.getName());
        existingImage.setFormat(image.getFormat());
        existingImage.setData(image.getData());
        existingImage.setDatasetId(image.getDatasetId());
        return imageRepository.save(existingImage);
    }

//    public boolean userContainsAuthorityToEdit(String datasetId, String userId){
//        return false;
//        //return datasetService.userContainsAuthorityToEdit(datasetId,userId);
//    }

    public Image deleteImage(String id) {
        Image deleted = imageRepository.findById(id)
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));

        //TODO: add throw UnsupportedOperationException if points list not empty
        if (!imageObjectService.findAllByImageId(id).isEmpty()) {
            throw new UnsupportedOperationException("Objects list of this image is not empty.");
        }

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
