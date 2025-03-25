package vttp.final_project.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vttp.final_project.configuration.jwtToken.JwtFilter;

@Configuration
public class SecurityConfig {

    @Value("${spring.security.user.name}")
    private String SPRING_SECURITY_USER_NAME;

    @Value("${spring.security.user.password}")
    private String SPRING_SECURITY_USER_PASSWORD;

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Configure HTTP security
        http
                // Disable CSRF for REST APIs
                .csrf(csrf -> csrf.disable())

                // Set stateless session management (no sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure request authorization
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(  "/", "/index.html", "/favicon.ico",
                                "/assets/**",
                                "/styles*.css", "/main*.js", "/runtime*.js",
                                "/polyfills*.js", "/scripts*.js","/gym.jpg","/manifest.json",
                                "/**.png", "/**.jpg", "/**.jpeg","/icons/**"
                        ).permitAll()
                        // Public endpoints that don't require authentication
                        .requestMatchers("/calendar/callback").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/meals/*/recipe").permitAll() // Specifically allow recipe endpoints

                        // Protected endpoints that require authentication
                        .requestMatchers("/api/meals/**").authenticated()
                        .requestMatchers("/api/mealplans/**").authenticated()
                        .requestMatchers("/api/user/**").authenticated()
                        .requestMatchers("/api/calendar/**").authenticated()
                        .requestMatchers("/api/payment/**").authenticated()
                        .requestMatchers("/api/workouts/**").authenticated()
                        .anyRequest().authenticated()
                )

                // Add JWT authentication filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
