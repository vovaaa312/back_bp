package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.model.request.authentication.AuthRequest;
import project.model.request.authentication.RegisterRequest;
import project.model.response.AuthResponse;
import project.service.repository.AuthUserRepository;
import project.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AuthController {

    private final AuthService authService;
    private final AuthUserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request)  {
        if (userRepository.findAuthUserByUsername(request.getUsername()).isPresent()) {
            String errorMessage = "User with username: {" + request.getUsername() + "} already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(errorMessage));
        }
        if(userRepository.findAuthUserByEmail(request.getEmail()).isPresent()){
            String errorMessage = "User with email: {" + request.getEmail() + "} already exists";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(errorMessage));

        }
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));

    }

//    @PreAuthorize("hasAnyAuthority('admin:read')")
//    @GetMapping("/hello")
//    public String HelloResponse(){
//        return "Hello";
//    }
//    @PreAuthorize("hasAnyAuthority('admin:read', 'admin:update')")
//    @GetMapping("/jwt")
//    public ResponseEntity<?> JwtResponse(@RequestBody String jwt){
//        JwtService jwtService = new JwtService();
//        String jwtDecode = jwtService.extractUserName(jwt);
//        return ResponseEntity.ok(userRepository.findAuthUsersByUsername(jwtDecode));
//    }
}
