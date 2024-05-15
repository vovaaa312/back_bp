package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.model.dataset.Dataset;
import project.model.dataset.UserDataset;
import project.model.dataset.UserDatasetDetails;
import project.model.exception.DatasetNotFoundException;
import project.model.exception.ProjectNotFoundException;
import project.model.exception.UserNotFoundException;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.repository.AuthUserRepository;
import project.service.repository.DatasetRepository;
import project.service.repository.ImageRepository;
import project.service.repository.ProjectRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final AuthUserRepository userRepository;
    private final UserDatasetService userDatasetService;
    private final ProjectRepository projectRepository;
    private final ImageRepository imageRepository;

    public List<Dataset> findAll() {
        return datasetRepository.findAll();
    }

    public List<Dataset> findAllByName(String name) {
        return datasetRepository.findAllByName(name).orElse(Collections.emptyList());
    }

    public List<Dataset> findAllByOwnerId(String ownerId) {
        return datasetRepository.findAllByOwnerId(ownerId).orElse(Collections.emptyList());
    }

    public List<Dataset> findAllByProjectId(String projectId) {
        projectRepository.findById(projectId).orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        return datasetRepository.findDatasetsByProjectId(projectId).orElse(Collections.emptyList());
    }

    public List<Dataset> findAllByCategory(String category) {
        return datasetRepository.findAllByCategory(category).orElse(Collections.emptyList());
    }

    public List<Dataset> findAllByCategoryAndProjectId(String category, String projectId) {
        return datasetRepository.findAllByCategoryAndProjectId(category, projectId).orElse(Collections.emptyList());
    }

    public Dataset findById(String id) {
        return datasetRepository.findById(id).orElseThrow(() -> new DatasetNotFoundException("Dataset not found."));
    }

    public Dataset saveDataset(Dataset dataset) {
        projectRepository.findById(dataset.getProjectId()).orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        userRepository.findById(dataset.getOwnerId()).orElseThrow(() -> new UserNotFoundException("User not found."));

        dataset.setCreationTimestamp(new Date());
        Dataset saved = datasetRepository.save(dataset);

        UserDataset userDataset = new UserDataset(null, saved.getOwnerId(), saved.getId(), SystemRole.DATASET_OWNER);
        addUserToDataset(userDataset);

        return saved;
    }

    public UserDataset addUserToDataset(UserDataset userDataset) {
        datasetRepository.findById(userDataset.getDatasetId()).orElseThrow(() -> new DatasetNotFoundException("Dataset not found."));
        userRepository.findById(userDataset.getUserId()).orElseThrow(() -> new UserNotFoundException("User not found."));

        return userDatasetService.save(userDataset);
    }

    public Dataset updateDataset(Dataset dataset) {
        projectRepository.findById(dataset.getProjectId()).orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        datasetRepository.findById(dataset.getId()).orElseThrow(() -> new DatasetNotFoundException("Dataset not found."));
        userRepository.findById(dataset.getOwnerId()).orElseThrow(() -> new UserNotFoundException("User not found."));

        Dataset existingDataset = datasetRepository.findById(dataset.getId()).get();
        existingDataset.setName(dataset.getName());
        existingDataset.setProjectId(dataset.getProjectId());
        existingDataset.setOwnerId(dataset.getOwnerId());

        return datasetRepository.save(existingDataset);
    }

    public Dataset deleteDataset(String id) {
        Dataset dataset = datasetRepository.findById(id).orElseThrow(() -> new DatasetNotFoundException("Dataset not found."));
//        if (!imageRepository.findAllByDatasetId(id).isEmpty()) {
//            throw new UnsupportedOperationException("Images list of this project is not empty.");
//        }
        imageRepository.deleteAllByDatasetId(dataset.getId());
        userDatasetService.deleteAllByDatasetId(id);
        datasetRepository.deleteById(id);

        return dataset;
    }

    public List<Dataset> deleteAllByProjectId(String projectId) {

        //List<Dataset> deletedDatasets = new ArrayList<>();
        List<Dataset> datasets = datasetRepository.findDatasetsByProjectId(projectId).orElse(Collections.emptyList());

        try {
            datasets.forEach(dataset -> {
                if (imageRepository.findAllByDatasetId(dataset.getId()).isEmpty()) {
                    //deletedDatasets.add(dataset);
                    //datasetRepository.deleteById(dataset.getId());
                    userDatasetService.deleteAllByDatasetId(dataset.getId());
                    imageRepository.deleteAllByDatasetId(dataset.getId());
                }
            });
        } catch (NullPointerException ignored) {

        }


        return datasetRepository.deleteAllByProjectId(projectId).orElse(Collections.emptyList());
    }

    public List<AuthUser> findUsersByDatasetId(String datasetId) {
        return userDatasetService.findUsersByDatasetId(datasetId);
    }

    public UserDataset deleteUserFromDataset(String datasetId, String userId) {
        UserDataset userDataset = findUserDatasetByDatasetIdAndUserId(datasetId, userId);
        userDatasetService.deleteById(userDataset.getId());

        return userDataset;
    }

    public UserDataset findUserDatasetByDatasetIdAndUserId(String datasetId, String userId) {
        return userDatasetService.findFirstByDatasetIdAndUserId(datasetId, userId);
    }

    public boolean userContainsAuthorityToEdit(String datasetId, String userId) {
        return userDatasetService.userContainsAuthorityToEdit(datasetId, userId);
    }

    public List<UserDatasetDetails> getUserDatasetDetailsByDatasetId(String datasetId) {

        return mapUserDetails(
                userDatasetService.findUsersByDatasetId(datasetId),
                userDatasetService.findAllByDatasetId(datasetId));
    }

    private List<UserDatasetDetails> mapUserDetails(
            List<AuthUser> datasetUsers,
            List<UserDataset> userDatasets) {
        return userDatasets.stream()
                .map(userDataset -> {
                    AuthUser user = datasetUsers.stream()
                            .filter(u -> u.getId().equals(userDataset.getUserId()))
                            .findFirst()
                            .orElse(null);
                    return new UserDatasetDetails(user, userDataset);

                })
                .collect(Collectors.toList());
    }

}

//public class DatasetService {
//
//    private final AuthUserService userService;
//    private final ProjectRepository projectRepository;
//    private final DatasetRepository datasetRepository;
//    private final UserDatasetService userDatasetService;
//
//    public Optional<List<Dataset>> findAll() {
//        return Optional.of(datasetRepository.findAll());
//    }
//
//    public Optional<List<Dataset>> findAllByName(String name) {
//        return datasetRepository.findAllByName(name);
//    }
//    public Optional<List<Dataset>>findAllByOwnerId(String ownerId){
//        return datasetRepository.findAllByOwnerId(ownerId);
//    }
//
//    public Optional<List<Dataset>> findAllByProjectId(String projectId) {
//        if (projectRepository.findById(projectId).isEmpty()) throw new DatasetNotFoundException("Project not found.");
//        return datasetRepository.findDatasetsByProjectId(projectId);
//    }
//
//    public Optional<Dataset> findById(String id) {
//        return datasetRepository.findById(id);
//    }
//
//    public Dataset saveDataset(Dataset dataset) {
//        if (projectRepository.findById(dataset.getProjectId()).isEmpty())
//            throw new ProjectNotFoundException("Project not found.");
//        if (userService.findAuthUserById(dataset.getOwnerId()).isEmpty())
//            throw new UserNotFoundException("User not found.");
//        // Project project = projectRepository.findById(dataset.getProjectId()).get();
//
//        dataset.setCreationTimestamp(new Date(System.currentTimeMillis()));
//        Dataset save = datasetRepository.save(dataset);
//
//        UserDataset userDataset = new UserDataset(
//                null,
//                save.getOwnerId(),
//                save.getId(),
//                SystemRole.DATASET_OWNER);
////
////        userDataset.setUserId(save.getOwnerId());
////        userDataset.setDatasetId(save.getId());
////        userDataset.setUserRole(SystemRole.DATASET_OWNER);
//
//        addUserToDataset(userDataset);
//
//
//        return save;
//    }
//
//    public UserDataset addUserToDataset(UserDataset userDataset) {
//        if (datasetRepository.findById(userDataset.getDatasetId()).isEmpty())
//            throw new DatasetNotFoundException("Dataset not found.");
//        if (userService.findAuthUserById(userDataset.getUserId()).isEmpty())
//            throw new UserNotFoundException("User not found.");
//
//        return userDatasetService.save(userDataset);
//    }
//
////
//
//    public Dataset updateDataset(Dataset dataset) {
//        if (projectRepository.findById(dataset.getProjectId()).isEmpty())
//            throw new DBObjectNotFoundException("Project not found.");
//        if (datasetRepository.findById(dataset.getId()).isEmpty())
//            throw new DatasetNotFoundException("Dataset not found.");
//        if (userService.findAuthUserById(dataset.getOwnerId()).isEmpty())
//            throw new UserNotFoundException("User not found.");
//
//        Dataset existingDataset = datasetRepository.findById(dataset.getId()).get();
//        existingDataset.setName(dataset.getName());
//        existingDataset.setProjectId(dataset.getProjectId());
//        existingDataset.setOwnerId(dataset.getOwnerId());
//        return datasetRepository.save(existingDataset);
//    }
//
//    public Dataset deleteDataset(String id) {
//        Dataset deleted = datasetRepository.findById(id).get();
//        //TODO: add throw UnsupportedOperationException if images list not empty
//
//        userDatasetService.deleteAllByDatasetId(id);
//        datasetRepository.deleteById(id);
//        return deleted;
//    }
//
//
//    public Optional<List<Dataset>> deleteAllByProjectId(String projectId) {
//        List<Dataset> datasets = datasetRepository.findDatasetsByProjectId(projectId).get();
//        datasets.forEach(dataset ->
//                userDatasetService.deleteAllByDatasetId(dataset.getId()));
//        return datasetRepository.deleteAllByProjectId(projectId);
//    }
//
//    public boolean isDatasetOwner(String datasetId, String userId) {
//        return userDatasetService.isDatasetOwner(datasetId, userId);
//    }
//
//    public List<AuthUser> findUsersByDatasetId(String datasetId) {
//
//
//
//        return userDatasetService.findUsersByDatasetId(datasetId);
//    }
//
//    public UserDataset deleteUserFromDataset(String datasetId, String userId) {
//        String deleted = userDatasetService.findByDatasetIdAndUserId(datasetId, userId).get().getId();
//        return userDatasetService.deleteById(deleted);
//    }
//
//    public Optional< UserDataset> findUserDatasetByDatasetIdAndUserId(String datasetId, String userId) {
//
//        return userDatasetService.findByDatasetIdAndUserId(datasetId, userId);
//    }
//
//    public boolean userContainsAuthorityToEdit(String datasetId, String userId) {
//        return userDatasetService.userContainsAuthorityToEdit(datasetId, userId);
//
//    }
//
////    public List<AuthUser> findUsersByDatasetId(String datasetId){
////        List<UserDataset> usersDatasets = userDatasetService.findAllByDatasetId(datasetId);
////        List<String> userIds = usersDatasets.stream()
////                .map(UserDataset::getUserId)
////                .collect(Collectors.toList());
////
////        List<AuthUser> users = userService.getUsersByIds(userIds);
////        return users;
////    }
//
////    public boolean isDatasetOwner(String userId, String datasetId) {
////        Optional<Dataset> datasetOpt = datasetRepository.findById(datasetId);
////        if(datasetOpt.isEmpty()) return false;
////
////
////        if()
////        return true;
////    }
//}
