package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.model.exception.DBObjectNotFoundException;
import project.model.project.UserProject;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.repository.UserProjectRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserProjectService {

    private final UserProjectRepository userProjectRepository;
    private final AuthUserService userService;

    public List<UserProject> findAll() {
        return userProjectRepository.findAll();
    }

    public UserProject findUserProjectByProjectIdAndUserId(String projectId, String userId) {
        return userProjectRepository.findFirstByProjectIdAndUserId(projectId, userId)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Project link not found."));

    }

    public UserProject findById(String id) {
        return userProjectRepository.findById(id)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Project link not found."));

    }

    public List<UserProject> findUserProjectsByProjectId(String projectId) {
        return userProjectRepository.findUserProjectsByProjectId(projectId)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Project link not found."));

    }

    public List<UserProject> findUserProjectsByUserId(String userId) {
        return userProjectRepository.findUserProjectsByUserId(userId)
                .orElseThrow(()-> new DBObjectNotFoundException("User-Project link not found."));

    }

    public void deleteAllByProjectId(String projectId) {
        userProjectRepository.deleteAllByProjectId(projectId);
    }

    public UserProject save(UserProject userProject) {
        userProjectRepository.findFirstByProjectIdAndUserId(userProject.getProjectId(), userProject.getUserId())
                .ifPresent(s -> {
                    throw new UnsupportedOperationException("User-project link already exists.");
                });
        return userProjectRepository.save(userProject);
    }

    public UserProject update(UserProject userProject) {
        UserProject existing = userProjectRepository.findById(userProject.getId())
                .orElseThrow(() -> new DBObjectNotFoundException("User-Project link not found."));

        if (userProjectRepository.findFirstByProjectIdAndUserId(userProject.getProjectId(), userProject.getUserId()).isPresent()) {
            throw new UnsupportedOperationException("User-project link already exists.");
        }

        existing.setProjectId(userProject.getProjectId());
        existing.setUserId(userProject.getUserId());
        existing.setUserRole(userProject.getUserRole());
        return userProjectRepository.save(existing);
    }

    public UserProject deleteById(String projectId) {
        UserProject userProject = userProjectRepository.findById(projectId)
                .orElseThrow(() -> new DBObjectNotFoundException("User-Project link not found."));

        if (userProject.getUserRole() == SystemRole.PROJECT_OWNER) {
            throw new UnsupportedOperationException("Project owner cannot be deleted.");
        }

        userProjectRepository.deleteById(projectId);
        return userProject;
    }

    public List<AuthUser> findUsersByProjectId(String projectId) {
        List<UserProject> userProjects = userProjectRepository.findUserProjectsByProjectId(projectId)
                                .orElseThrow(() -> new DBObjectNotFoundException("User-Project links not found."));
        List<String> userIds = usersString(userProjects);
        return userService.getUsersByIds(userIds);

    }

    private List<String> usersString(List<UserProject> userProjects) {
        return userProjects.stream()
                .map(UserProject::getUserId)
                .collect(Collectors.toList());
    }

    public boolean userContainsAuthorityToEdit(String projectId, String userId) {
        return userProjectRepository
                .findFirstByProjectIdAndUserId(projectId, userId)
                .map(UserProject::getUserRole)
                .filter(role -> role == SystemRole.PROJECT_OWNER || role == SystemRole.PROJECT_DATASET)
                .isPresent();
    }
}


//public class UserProjectService {
//
//    private final UserProjectRepository userProjectRepository;
//    private final AuthUserService userService;
//
//
//    public Optional<List<UserProject>> findAll() {
//        return Optional.of(userProjectRepository.findAll());
//    }
//
//    public Optional<UserProject> findUserProjectByProjectIdAndUserId(String projectId, String userId) {
//        return userProjectRepository.findFirstByProjectIdAndUserId(projectId, userId);
//
//    }
//
//    public Optional<UserProject> findById(String id) {
//        return userProjectRepository.findById(id);
//    }
//
//    public Optional<List<UserProject>> findUserProjectsByProjectId(String projectId) {
//        return userProjectRepository.findUserProjectsByProjectId(projectId);
//    }
//
//    public Optional<List<UserProject>> findUserProjectsByUserId(String userId) {
//        return userProjectRepository.findUserProjectsByUserId(userId);
//    }
//
//    public Optional<List<UserProject>> deleteAllByProjectId(String projectId) {
//
//        return userProjectRepository.deleteAllByProjectId(projectId);
//    }
//
//
//    public UserProject save(UserProject userProject) {
//        if (userProjectRepository.findFirstByProjectIdAndUserId(userProject.getProjectId(), userProject.getUserId()).isPresent()) {
//            throw new UnsupportedOperationException("User-project link already exists.");
//        }
//        return userProjectRepository.save(userProject);
//    }
//
//    public UserProject update(UserProject userProject) {
//        UserProject existingUserProject = userProjectRepository.findById(userProject.getProjectId()).get();
//        existingUserProject.setProjectId(userProject.getProjectId());
//        existingUserProject.setUserId(userProject.getUserId());
//        existingUserProject.setUserRole(userProject.getUserRole());
//        if (userProjectRepository.findFirstByProjectIdAndUserId(existingUserProject.getProjectId(), existingUserProject.getUserId()).isPresent()) {
//            throw new UnsupportedOperationException("User-project link already exists.");
//        }
//        return userProjectRepository.save(existingUserProject);
//    }
//
//    public UserProject deleteById(String projectId) {
//        Optional<UserProject> userProjectOpt = userProjectRepository.findById(projectId);
//        if (userProjectOpt.isEmpty())
//            throw new DBObjectNotFoundException("UserProject not found.");
//        UserProject deleted = userProjectOpt.get();
//        if (deleted.getUserRole().equals(SystemRole.PROJECT_OWNER))
//            throw new UnsupportedOperationException("Project owner cannot be deleted.");
//
//        userProjectRepository.deleteById(projectId);
//
//        return deleted;
//    }
//
////    public boolean isProjectOwner(String userId, String projectId) {
////        UserProject userProject = userProjectRepository
////                .findUserProjectByProjectIdAndUserId(projectId, userId).get();
////        return userProject != null && userProject.getUserRole() == SystemRole.PROJECT_OWNER;
////    }
//
//
//    public List<AuthUser> findUsersByProjectId(String projectId) {
//        List<UserProject> userProjects = userProjectRepository.findUserProjectsByProjectId(projectId).get();
//
////        List<String> userIds = userProjects.stream()
////                .map(UserProject::getUserId)
////                .collect(Collectors.toList());
//
//        List<String> userIds = usersString(userProjects);
//
//        return userService.getUsersByIds(userIds);
//
//    }
//
//    private List<String> usersString(List<UserProject> userProjects) {
//        return userProjects.stream()
//                .map(UserProject::getUserId)
//                .collect(Collectors.toList());
//    }
//
////    public boolean containsAuthorityToEdit(String projectId, String userId) {
////        Optional<UserProject> find = userProjectRepository.findUserProjectByProjectIdAndUserId(projectId,userId);
////        if(find.isEmpty())return false;
////        SystemRole role = find.get().getUserRole();
////        return role.equals(SystemRole.PROJECT_OWNER) ||
////                role.equals(SystemRole.PROJECT_DATASET);
////    }
//
//    public boolean userContainsAuthorityToEdit(String projectId, String userId) {
//        return userProjectRepository
//                .findFirstByProjectIdAndUserId(projectId, userId)
//                .map(UserProject::getUserRole)
//                .filter(role -> role.equals(SystemRole.PROJECT_OWNER) || role.equals(SystemRole.PROJECT_DATASET))
//                .isPresent();
//    }
//
//}
