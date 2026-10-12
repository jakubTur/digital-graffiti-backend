package edu.w04ist.si041g.backend.user;

import edu.w04ist.si041g.backend.user.dto.UserRequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    public UserService() {}

    public User createUser(UserRequestBody user) {
        User newUser = new User();
        newUser.setNickname(user.getNickname());
        newUser.setActive(user.isActive());
        newUser.setVerified(user.isVerified());
        newUser.setEmail(user.getEmail());
        newUser.setCreatedAt(Instant.now());
        newUser.setRole(UserRole.valueOf(user.getRole()));
        return userRepository.save(newUser);
    }

    public User updateUser(UserRequestBody user, Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        existingUser.setRole(UserRole.valueOf(user.getRole()));
        existingUser.setActive(user.isActive());
        existingUser.setVerified(user.isVerified());
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(existingUser);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
