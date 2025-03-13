package vttp.final_project.services.UserManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vttp.final_project.models.User;
import vttp.final_project.repository.UserRepository;
import vttp.final_project.repository.UserSqlRepository;

@Service
public class CustomDetailService implements UserDetailsService {

    @Autowired
    private UserSqlRepository userSqlRepo;

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userSqlRepo.findByEmail(email);
        if (user == null) {
            user = userRepo.findByEmail(email);
        }
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword()) // Ensure password is hashed
                .roles("USER") // Modify roles as needed
                .build();
    }
}
