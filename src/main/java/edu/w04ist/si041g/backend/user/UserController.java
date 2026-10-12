package edu.w04ist.si041g.backend.user;

import edu.w04ist.si041g.backend.user.dto.UserRequestBody;
import edu.w04ist.si041g.backend.user.dto.UserResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@RequestBody UserRequestBody user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId())).build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@RequestBody UserRequestBody user, @PathVariable Long id) {
        User updatedUser = userService.updateUser(user, id);
        return ResponseEntity.ok(updatedUser);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseBody> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(toUserResponseBody(user));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserResponseBody>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponseBody> responseBodies = users.stream()
                .map(this::toUserResponseBody)
                .toList();
        return ResponseEntity.ok(responseBodies);
    }

    private UserResponseBody toUserResponseBody(User user) {
        return new UserResponseBody(
            user.getNickname(),
            user.isActive(),
            user.isVerified(),
            user.getEmail(),
            user.getRole().name()
        );
    }
}
