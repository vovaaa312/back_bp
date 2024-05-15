package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.model.dataset.UserDataset;
import project.model.exception.DBObjectNotFoundException;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.repository.AuthUserRepository;
import project.service.repository.UserDatasetRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserDatasetService {
    private final UserDatasetRepository userDatasetRepository;
    private final AuthUserRepository userService;

    public List<UserDataset> findAll() {
        return userDatasetRepository.findAll();
    }

    public UserDataset findFirstByDatasetIdAndUserId(String datasetId, String userId) {
        return userDatasetRepository.findFirstByDatasetIdAndUserId(datasetId, userId)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Dataset link not found."));
    }

    public UserDataset findById(String id) {
        return userDatasetRepository.findById(id)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Dataset link not found."));
    }

    public List<UserDataset> findAllByDatasetId(String datasetId) {
        return userDatasetRepository.findAllByDatasetId(datasetId)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Dataset links not found."));
    }

    public List<UserDataset> findAllByUserId(String userId) {
        return userDatasetRepository.findAllByUserId(userId)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Dataset links not found."));
    }

    public UserDataset save(UserDataset userDataset) {
        userDatasetRepository.findFirstByDatasetIdAndUserId(userDataset.getDatasetId(), userDataset.getUserId())
                .ifPresent(s->{
                    throw new UnsupportedOperationException("User-dataset link already exists.");
                });
        return userDatasetRepository.save(userDataset);
    }

    public UserDataset update(UserDataset userDataset) {
        UserDataset existingUserDataset = userDatasetRepository.findById(userDataset.getId()).get();
        existingUserDataset.setUserId(userDataset.getUserId());
        existingUserDataset.setDatasetId(userDataset.getDatasetId());
        existingUserDataset.setUserRole(userDataset.getUserRole());
        return userDatasetRepository.save(existingUserDataset);
    }

    public UserDataset deleteById(String id) {
        UserDataset deleted = userDatasetRepository.findById(id).get();
        if (deleted.getUserRole().equals(SystemRole.DATASET_OWNER))
            throw new UnsupportedOperationException("Dataset owner cannot be deleted.");
        userDatasetRepository.deleteById(id);
        return deleted;
    }

    public Optional<List<UserDataset>> deleteAllByDatasetId(String id) {
        return userDatasetRepository.deleteUserDatasetsByDatasetId(id);
    }

    public boolean isDatasetOwner(String datasetId, String userId) {
        UserDataset userDataset = userDatasetRepository
                .findFirstByDatasetIdAndUserId(datasetId, userId)
                .orElseThrow();
        return userDataset != null && userDataset.getUserRole() == SystemRole.DATASET_OWNER;
    }

    public List<AuthUser> findUsersByDatasetId(String datasetId) {
        List<UserDataset> userDatasets = userDatasetRepository.findAllByDatasetId(datasetId)
                .orElseThrow(() -> new DBObjectNotFoundException("User-Dataset links not found."));
        List<String>userIds = usersString(userDatasets);
        return userService.findAllById(userIds);

    }

    private List<String> usersString(List<UserDataset> userDatasets) {
        return userDatasets.stream()
                .map(UserDataset::getUserId)
                .collect(Collectors.toList());
    }

    public boolean userContainsAuthorityToEdit(String datasetId, String userId) {
        return userDatasetRepository
                .findFirstByDatasetIdAndUserId(datasetId, userId)
                .map(UserDataset::getUserRole)
                .filter(role -> role.equals(SystemRole.DATASET_OWNER) || role.equals(SystemRole.DATASET_LABEL))
                .isPresent();
    }

}
