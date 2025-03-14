package vttp.final_project.repository.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vttp.final_project.models.user.User;

@Repository
public class UserRepository {

    @Autowired
    @Qualifier("redis-object")
    private RedisTemplate<String,Object> template;

    private final String KEY = "USER";

    // HGETALL USER to get userEmail and Encrypted Password
    // Save a user
    public void save(User user) {
        template.opsForHash().put(KEY,user.getEmail(),user);
    }

    // Retrieve a user by email
    public User findByEmail(String email) {
        return (User) template.opsForHash().get(KEY, email);
    }

    // Random key for healthcheck
    public String getRandomKey() {
        try {
            return (String) template.randomKey();
        } catch (Exception e) {
            System.err.println("Error fetching random key: " + e.getMessage());
            return null;
        }
    }
}
