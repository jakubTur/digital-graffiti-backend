package edu.w04ist.si041g.backend.user;

import edu.w04ist.si041g.backend.post.PostRepository;
import edu.w04ist.si041g.backend.user.dto.UserRequestBody;
import edu.w04ist.si041g.backend.user.dto.UserUpdateBody;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;
    public UserService() {}

    public User createUser(UserRequestBody user) {
        User newUser = new User();
        newUser.setNickname(user.getNickname());
        newUser.setActive(true);
        newUser.setVerified(false);
        newUser.setEmail(user.getEmail());
        newUser.setCreatedAt(Instant.now());
        newUser.setRole(UserRole.user);
        return userRepository.save(newUser);
    }

    public User createAdmin(UserRequestBody admin) {
        User newAdmin = new User();
        newAdmin.setNickname(admin.getNickname());
        newAdmin.setActive(true);
        newAdmin.setVerified(true);
        newAdmin.setEmail(admin.getEmail());
        newAdmin.setCreatedAt(Instant.now());
        newAdmin.setRole(UserRole.admin);
        return userRepository.save(newAdmin);
    }

    public User updateUser(UserUpdateBody user, Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getBio() != null) {
            existingUser.setBio(user.getBio());
        }
        existingUser.setActive(user.isActive());
        existingUser.setVerified(user.isVerified());
        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        postRepository.deleteAllByUserId_Id(id);
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
