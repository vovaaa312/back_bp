package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.model.exception.DBObjectNotFoundException;
import project.model.image.Image;
import project.service.repository.DatasetRepository;
import project.service.repository.ImageRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final DatasetRepository datasetRepository;

    public List<Image> findAll() {
        return imageRepository.findAll();
    }

    public Optional<List<Image>> findImagesByName(String name) {
        return imageRepository.findAllByName(name);
    }

    public Optional<List<Image>> findImagesByDatasetId(String datasetId) {
        return imageRepository.findAllByDatasetId(datasetId);
    }

    public Image findById(String id) {
        return imageRepository.findById(id).get();
    }

    public Image saveImage(Image image){
        if(datasetRepository.findById(image.getDatasetId()).isEmpty())
            throw new DBObjectNotFoundException("Dataset not found.");
        return imageRepository.save(image);
    }
    public Image updateImage(Image image) {
        Image existingImage = imageRepository.findById(image.getId()).get();
        if (datasetRepository.findById(existingImage.getDatasetId()).isEmpty())
            throw new DBObjectNotFoundException("Dataset not found.");

        existingImage.setName(image.getName());
        existingImage.setFormat(image.getFormat());
        existingImage.setData(image.getData());
        existingImage.setDatasetId(image.getDatasetId());
        return imageRepository.save(existingImage);
    }

    public Image deleteImage(String id){
        Image deleted = imageRepository.findById(id).get();
        //TODO: add throw UnsupportedOperationException if points list not empty

        imageRepository.deleteById(id);
        return deleted;
    }


}
