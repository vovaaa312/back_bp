package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import project.model.dataset.Dataset;
import project.model.dataset.UserDataset;
import project.model.dataset.UserDatasetDetails;
import project.model.exception.DBObjectNotFoundException;
import project.model.exception.ProjectNotFoundException;
import project.model.exception.UserNotFoundException;
import project.model.project.Project;
import project.model.project.UserProject;
import project.model.project.UserProjectDetails;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.repository.DatasetRepository;
import project.service.repository.ProjectRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final AuthUserService userService;
    private final ProjectRepository projectRepository;

    private final DatasetRepository datasetRepository;
    private final UserProjectService userProjectService;

    //private final JwtService jwtService;

    /**
     * List<Project> findAll()
     * return list of all projects in database
     **/
    public Optional<List<Project>> findAll() {
        return Optional.of(projectRepository.findAll());
    }

    /**
     * List<Project> findProjectsByOwnerId(String id)
     * return list of all projects in database by owner id
     **/
    public List<Project> findProjectsByOwnerId(String id) {
        userService.findAuthUserById(id);
        return projectRepository.findProjectsByOwnerId(id)
                .orElseThrow(()-> new ProjectNotFoundException("Project not found."));
    }

    /**
     * List<Project> findById(String projectId) - return project by id
     **/
    public Project findById(String projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(()-> new ProjectNotFoundException("Project not found."));
    }

    /**
     * Project saveProject(Project project)
     * save project to database
     **/
    public Project save(Project project) {

//        if (userService.findAuthUserById(project.getOwnerId()).isEmpty())
//            throw new DBObjectNotFoundException("User not found.");

        userService.findAuthUserById(project.getOwnerId())
                .orElseThrow(() -> new DBObjectNotFoundException("User not found."));

        project.setCreationTimestamp(new Date(System.currentTimeMillis()));
        Project save = projectRepository.save(project);

        UserProject userProject = new UserProject();
        userProject.setProjectId(project.getId());
        userProject.setUserId(project.getOwnerId());
        userProject.setUserRole(SystemRole.PROJECT_OWNER);
        addUserToProject(userProject);

        return save;
    }


    /**
     * Project updateProject(Project project)
     * update project
     **/
    public Project update(Project project) {
        Project existingProject = projectRepository.findById(project.getId())
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        userService.findAuthUserById(project.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));

        existingProject.setName(project.getName());
        existingProject.setOwnerId(project.getOwnerId());
        //existingProject.setCreationTimestamp(new Date()); //or project.getCreationTimestamp()

        return projectRepository.save(existingProject);
    }

    /**
     * Project deleteProject(String id)
     * delete project
     **/
//    public Project deleteById(String id) {
//
////        if (projectRepository.findById(id).isEmpty())
////            throw new ProjectNotFoundException("Project not found.");
//
//        Project deleted = projectRepository.findById(id)
//                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));
//
////
////        Optional<List<Dataset>> datasetsOpt = datasetRepository.findDatasetsByProjectId(id);
////        if (datasetsOpt.isPresent() && !datasetsOpt.get().isEmpty())
////            throw new UnsupportedOperationException("Dataset list of this project is not empty.");
//
//        if (!datasetRepository.findDatasetsByProjectId(id).isEmpty()) {
//            throw new UnsupportedOperationException("Dataset list of this project is not empty.");
//        }
//        //Project deleted = projectRepository.findById(id).get();
//
//        projectRepository.deleteById(id);
//        userProjectService.deleteAllByProjectId(id);
//
//        return deleted;
//    }

    public Project deleteById(String id) {
        if (projectRepository.findById(id).isEmpty())
            throw new ProjectNotFoundException("Project not found.");

        List<Dataset> datasets = datasetRepository.findDatasetsByProjectId(id).orElseThrow();
        if (!datasets.isEmpty())
            throw new UnsupportedOperationException("Datasets list of this project is not empty.");

        //datasets.forEach(dataset -> datasetRepository.delete(dataset)); // Optional: Force delete datasets if policy allows

        Project deleted = projectRepository.findById(id).get();
        projectRepository.deleteById(id);
        userProjectService.deleteAllByProjectId(id);

        return deleted;
    }

    public boolean userContainsAuthorityToEdit(String projectId, String userId) {
        return userProjectService.userContainsAuthorityToEdit(projectId, userId);
    }


    public List<UserProject> findUsersProjectsByProjectId(String projectId) {
//        if (projectRepository.findById(projectId).isEmpty())
//            throw new ProjectNotFoundException("Project not found.");

        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));

        return userProjectService.findUserProjectsByProjectId(projectId);
    }

    /**
     * UserProject addUserToProject(UserProject userProject)
     * save user to the project users list
     **/
    public UserProject addUserToProject(UserProject userProject) {
//        if (userService.findAuthUserById(userProject.getUserId()).isEmpty())
//            throw new UserNotFoundException("User not found.");
//        if (projectRepository.findById(userProject.getProjectId()).isEmpty())
//            throw new ProjectNotFoundException("Project not found.");

        userService.findAuthUserById(userProject.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        projectRepository.findById(userProject.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));


        return userProjectService.save(userProject);
    }

    public UserProject findUserProjectByProjectIdAndUserId(String projectId, String userId) {
//         if (projectRepository.findById(projectId).isEmpty())
//            throw new ProjectNotFoundException("Project not found.");
//        if (userService.findAuthUserById(userId).isEmpty())
//            throw new UserNotFoundException("User not found.");

        projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found."));
        userService.findAuthUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found."));
        return userProjectService.findUserProjectByProjectIdAndUserId(projectId, userId);
    }

//    public UserProject deleteUserFromProject(String projectId, String userId) {
//        //  UserProject deleted = userProjectService.deleteById(userProject.getProjectId());
//        Optional<UserProject> deleted = userProjectService.findUserProjectByProjectIdAndUserId(projectId, userId);
//        if (deleted.isEmpty()) throw new DBObjectNotFoundException("");
//        return userProjectService.deleteById(deleted.get().getId());
//    }

    public UserProject deleteUserFromProject(String projectId, String userId) {
        UserProject userProject = findUserProjectByProjectIdAndUserId(projectId, userId);
        return userProjectService.deleteById(userProject.getId());
    }
    public List<UserProjectDetails> getUserProjectDetailsByProjectId(String projectId) {
        List<AuthUser> users = userProjectService.findUsersByProjectId(projectId);
        List<UserProject> userProjects = userProjectService.findUserProjectsByProjectId(projectId);

        return mapUserDetails(users,userProjects);
    }

    public List<UserProjectDetails> mapUserDetails(List<AuthUser> projectUsers,
                                                   List<UserProject> userProjects){
        return userProjects.stream()
                .map(userDataset -> {
                    AuthUser user = projectUsers.stream()
                            .filter(u -> u.getId().equals(userDataset.getUserId()))
                            .findFirst()
                            .orElse(null);
                    return new UserProjectDetails(user, userDataset);

                })
                .collect(Collectors.toList());
    }


    /**
     * boolean isProjectOwner(String userId, String projectId)
     * returns true if userId==project.getOwnerId()
     **/
//    public boolean isProjectOwner(String userId, String projectId) {
//        // Project project = projectRepository.findById(projectId).get();
//        return userProjectService.isProjectOwner(userId, projectId);
//    }

}
