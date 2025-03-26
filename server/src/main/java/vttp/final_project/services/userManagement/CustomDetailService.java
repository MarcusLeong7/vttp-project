package vttp.final_project.services.userManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vttp.final_project.models.userModels.User;
import vttp.final_project.repository.user.UserSqlRepository;

@Service
public class CustomDetailService implements UserDetailsService {
    /* Implements UserDetailsService interface from Spring Security*/
    @Autowired
    private UserSqlRepository userSqlRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userSqlRepo.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        // Convert User object to Spring Security's UserDetails object
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // Ensure password is hashed
                .roles("USER") // Modify roles as needed
                .build();
    }
}
