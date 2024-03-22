package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.model.AuthUser;
import project.model.Role;
import project.model.request.AuthRequest;
import project.model.request.RegisterRequest;
import project.model.response.AuthResponse;
import project.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public AuthResponse reqister(RegisterRequest request) {
        var user = AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .response(jwtToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
         authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                      request.getEmail(),
                      request.getPassword()
              )
         );
         var user = userRepository.findAuthUsersByEmail(request.getEmail())
                 .orElseThrow();

         var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .response(jwtToken)
                .build();
    }
}
