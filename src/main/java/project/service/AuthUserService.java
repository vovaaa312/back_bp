package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.model.exception.UserNotFoundException;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.service.repository.AuthUserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthUserService {
    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public Optional<List<AuthUser>> findAll() {
        return Optional.of(userRepository.findAll());
    }

    public Optional<AuthUser> findAuthUserByEmail(String email) {
        return userRepository.findAuthUserByEmail(email);
    }

    public Optional<AuthUser> findAuthUserByUsername(String username) {
        return userRepository.findAuthUserByUsername(username);
    }

    public Optional<AuthUser> findAuthUserById(String id) {
        return userRepository.findAuthUsersById(id);
    }

    public List<AuthUser> getUsersByIds(List<String> userIds) {
        return userRepository.findAllById(userIds);
    }

    public Optional<AuthUser> addUser(AuthUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return Optional.of(userRepository.save(user));

    }

//    public Optional<AuthUser> updateAuthUser(AuthUser authUser) {
//        AuthUser existingUser = userRepository.findAuthUsersById(authUser.getId()).get();
//        existingUser.setUsername(authUser.getUsername());
//        existingUser.setEmail(authUser.getEmail());
//        existingUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
//        existingUser.setRole(authUser.getRole());
//        existingUser.setActive(authUser.isActive());
//        return Optional.of(userRepository.save(existingUser));
//    }

    public Optional<AuthUser> updateAuthUser(String userId, AuthUser authUser) {
        AuthUser existingUser = userRepository.findAuthUsersById(userId).get();
        existingUser.setUsername(authUser.getUsername());
        existingUser.setEmail(authUser.getEmail());
        existingUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
        existingUser.setRole(authUser.getRole());
        existingUser.setActive(authUser.isActive());
        return Optional.of(userRepository.save(existingUser));
    }


    public Optional<AuthUser> updateUsername(String userId, String newUsername) {
        if (userRepository.findAuthUsersById(userId).isEmpty()) throw new UserNotFoundException("User not found.");
        AuthUser existingUser = userRepository.findAuthUsersById(userId).get();
        existingUser.setUsername(newUsername);
        return Optional.of(userRepository.save(existingUser));

    }

    public Optional<AuthUser> updateEmail(String userId, String newEmail) {
        if (userRepository.findAuthUsersById(userId).isEmpty()) throw new UserNotFoundException("User not found.");

        AuthUser existingUser = userRepository.findAuthUsersById(userId).get();
        existingUser.setEmail(newEmail);
        return Optional.of(userRepository.save(existingUser));

    }

    public Optional<AuthUser> updatePassword(String userId, String newPassword) {
        if (userRepository.findAuthUsersById(userId).isEmpty()) throw new UserNotFoundException("User not found.");

        AuthUser existingUser = userRepository.findAuthUsersById(userId).get();
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        return Optional.of(userRepository.save(existingUser));
    }

    public Optional<AuthUser> updateRole(String userId, String role) {
        if (userRepository.findAuthUsersById(userId).isEmpty()) throw new UserNotFoundException("User not found.");

        AuthUser existingUser = userRepository.findAuthUsersById(userId).get();
        existingUser.setRole(SystemRole.valueOf(role));
        return Optional.of(userRepository.save(existingUser));
    }


    public Optional<AuthUser> deleteUser(String id) {
        if (userRepository.findAuthUsersById(id).isEmpty()) throw new UserNotFoundException("User not found.");

        AuthUser deleted = userRepository.findAuthUsersById(id).get();
        userRepository.deleteById(id);
        return Optional.of(deleted);
    }

}
