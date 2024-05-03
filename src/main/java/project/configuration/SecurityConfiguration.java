package project.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.*;
import static project.model.user.SystemPermission.*;
import static project.model.user.SystemRole.SYSTEM_ADMIN;

@Configuration
@EnableMongoRepositories
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration  {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;



    private static final String[] WHITE_LIST_URL = {
            "/api/auth/**",};
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf()
//                .disable()
//                .authorizeHttpRequests()
//                .requestMatchers("")
//                .permitAll()
//                .anyRequest()
//                .authenticated();
//        return http.build();

        http
                .csrf()
                .disable()
                .authorizeRequests()
                .requestMatchers(WHITE_LIST_URL)
                .permitAll()

//                .requestMatchers(GET,"/api/projects***").hasAnyRole(SYSTEM_ADMIN.name())
//                .requestMatchers(POST,"/api/projects***").hasAnyRole(SYSTEM_ADMIN.name())
//                .requestMatchers(PUT,"/api/projects***").hasAnyRole(SYSTEM_ADMIN.name())
//                .requestMatchers(DELETE,"/api/projects***").hasAnyRole(SYSTEM_ADMIN.name())
//
//                .requestMatchers(GET,"/api/datasets***").hasAnyRole(SYSTEM_ADMIN.name())


//                .requestMatchers("/api/test/***").hasAnyRole(SYSTEM_ADMIN.name(), SYSTEM_RESEARCHER.name())

                //.requestMatchers("/api/images/**").hasRole(ADMIN_READ.name())
//                .requestMatchers(PUT,"/api/test/***").hasAnyAuthority(ADMIN_UPDATE.name(), RESEARCHER_UPDATE.name())
//                .requestMatchers(DELETE,"/api/test/***").hasAnyAuthority(ADMIN_DELETE.name(), RESEARCHER_DELETE.name())

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final org.springframework.web.cors.CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("HEAD",
                "GET", "POST", "PUT", "DELETE", "PATCH"));
        // setAllowCredentials(true) is important, otherwise:
        // The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'.
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        // setAllowedHeaders is important! Without it, OPTIONS preflight request
        // will fail with 403 Invalid CORS request
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
