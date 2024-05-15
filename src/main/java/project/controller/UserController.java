package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.model.request.user_request.*;
import project.model.user.AuthUser;
import project.service.AuthUserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
@PreAuthorize("hasRole('SYSTEM_ADMIN')")

public class UserController {

    private final AuthUserService userService;

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @PreAuthorize("hasAuthority('admin:create')")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody AuthUser user){
        if (userService.findAuthUserByUsername(user.getUsername()).isPresent()) {
            String errorMessage = "User with username: {" + user.getUsername() + "} already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        if(userService.findAuthUserByEmail(user.getEmail()).isPresent()){
            String errorMessage = "User with email: {" + user.getEmail() + "} already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        return ResponseEntity.ok(userService.updateAuthUser(id,user).orElseThrow());
    }
    @PreAuthorize("hasAuthority('admin:create')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody AuthUser user){
        if (userService.findAuthUserByUsername(user.getUsername()).isPresent()) {
            String errorMessage = "User with username: {" + user.getUsername() + "} already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        if(userService.findAuthUserByEmail(user.getEmail()).isPresent()){
            String errorMessage = "User with email: {" + user.getEmail() + "} already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        return ResponseEntity.ok(userService.addUser(user).orElseThrow());
    }

    @PreAuthorize("hasAuthority('admin:update')")
    @PutMapping("/updateUsername")
    public ResponseEntity<Optional<?>> updateUsername(@RequestBody UpdateUsernameRequest request) {
        if (userService.findAuthUserById(request.getUserId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Optional.of("User was not found"));
        }
        if (userService.findAuthUserByUsername(request.getNewUsername()).isPresent()) {
            String message = "User with username '" + request.getNewUsername() + "' already exists";
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Optional.of(message));

        }

        return ResponseEntity.ok(userService.updateUsername(request.getUserId(), request.getNewUsername()));
    }

    @PreAuthorize("hasAuthority('admin:update')")
    @PutMapping("/updateEmail")
    public ResponseEntity<Optional<?>> updateEmail(@RequestBody UpdateEmailRequest request) {
        if (userService.findAuthUserById(request.getUserId()).isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Optional.of("User was not found"));
        }
        if (userService.findAuthUserByEmail(request.getNewEmail()).isPresent()) {
            String message = "User with email '" + request.getNewEmail() + "' already exists";
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Optional.of(message));

        }
        return ResponseEntity
                .ok(userService.updateEmail(request.getUserId(), request.getNewEmail()));
    }

    @PreAuthorize("hasAuthority('admin:update')")
    @PutMapping("/updatePassword")
    public ResponseEntity<Optional<?>> updatePassword(@RequestBody UpdatePasswordRequest request) {
        if (userService.findAuthUserById(request.getUserId()).isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Optional.of("User was not found"));
        }

        return ResponseEntity.ok(userService.updatePassword(request.getUserId(), request.getNewPassword()));
    }

    @PreAuthorize("hasAuthority('admin:update')")
    @PutMapping("/updateRole")
    public ResponseEntity<Optional<?>> updateRole(@RequestBody UpdateRoleRequest request) {
        if (userService.findAuthUserById(request.getUserId()).isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Optional.of("User was not found"));
        }

        return ResponseEntity
                .ok(userService
                        .updateRole(request.getUserId(), request.getNewRole()));
    }


    @PreAuthorize("hasAuthority('admin:delete')")
    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<Optional<?>> deleteUserById(@PathVariable String id) {
        return ResponseEntity.ok(Optional.of(
                userService.deleteUser(id)));
    }

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getById/{id}")
    public ResponseEntity<Optional<?>> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(Optional.of(
                userService.findAuthUserById(id)));
    }

    @PreAuthorize("hasAuthority('admin:read')")
    @GetMapping("/getByUsername/{username}")
    public ResponseEntity<Optional<?>> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(Optional.of(
                userService.findAuthUserByUsername(username)));
    }



}
