package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.model.exception.ImageNotFoundException;
import project.model.exception.ImageObjectNotFoundException;
import project.model.image.ImageObject;
import project.service.repository.ImageObjectRepository;
import project.service.repository.ImageRepository;
import project.service.repository.ObjectPointRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageObjectService {
    private final ImageObjectRepository objectRepository;
    private final ImageRepository imageRepository;
    private final ObjectPointRepository objectPointRepository;


    public List<ImageObject> findAll() {
        return objectRepository.findAll();
    }

    public List<ImageObject> findAllByImageId(String imageId) {
        return objectRepository.findAllByImageId(imageId);
    }

    public ImageObject findById(String id) {
        return objectRepository
                .findById(id)
                .orElseThrow(() -> new ImageObjectNotFoundException("Image object not found."));
    }

    public ImageObject save(ImageObject imageObject) {
        imageRepository.findById(imageObject.getImageId())
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));
        return objectRepository.save(imageObject);
    }

    public ImageObject update(ImageObject imageObject) {
        imageRepository.findById(imageObject.getImageId())
                .orElseThrow(() -> new ImageNotFoundException("Image not found."));

        ImageObject existed = findById(imageObject.getId());
        existed.setName(imageObject.getName());
        existed.setImageId(imageObject.getImageId());
        return null;
    }

    public ImageObject deleteById(String id) {
        ImageObject deleted = findById(id);
        objectRepository.deleteById(id);
        return deleted;
    }

    public List<ImageObject> deleteAllByImageId(String imageId) {
        imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageObjectNotFoundException("Image not found."));
        List<ImageObject> deleted = findAllByImageId(imageId);

        deleted.forEach(imageObject->{
            objectPointRepository.deleteAllByImageObjectId(imageObject.getId());
        });
        return objectRepository.deleteAllByImageId(imageId);
    }
}
