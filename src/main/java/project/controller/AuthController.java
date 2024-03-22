package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.model.request.AuthRequest;
import project.model.request.RegisterRequest;
import project.model.response.AuthResponse;
import project.repository.UserRepository;
import project.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request)  {
        if (userRepository.findAuthUsersByEmail(request.getEmail()).isPresent()) {
            String errorMessage = "User with email: {" + request.getEmail() + "} already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(errorMessage));
        }
        return ResponseEntity.ok(authService.reqister(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));

    }
}
