package project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import project.model.user.AuthUser;
import project.model.user.SystemRole;
import project.model.request.authentication.AuthRequest;
import project.model.request.authentication.RegisterRequest;
import project.model.response.AuthResponse;
import project.service.repository.AuthUserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    public AuthResponse register(RegisterRequest request) {
        var user = AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(SystemRole.SYSTEM_USER)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .jwtResponse(jwtToken)
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
         authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                      request.getUsername(),
                      request.getPassword()
              )
         );
         var user = userRepository.findAuthUserByUsername(request.getUsername())
                 .orElseThrow();

         var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .jwtResponse(jwtToken)
                .build();
    }


}
