package edu.w04ist.si041g.backend.user;

import edu.w04ist.si041g.backend.common.image.dto.PhotoResponseBody;
import edu.w04ist.si041g.backend.notification.NotificationStoreService;
import edu.w04ist.si041g.backend.storage.SupabaseStorageService;
import edu.w04ist.si041g.backend.user.dto.FirebaseToken;
import edu.w04ist.si041g.backend.user.dto.UserRequestBody;
import edu.w04ist.si041g.backend.user.dto.UserResponseBody;
import edu.w04ist.si041g.backend.user.dto.UserUpdateBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JdbcTemplate jdbc;
    private final SupabaseStorageService storage;
    private final NotificationStoreService notificationStoreService;
    public UserController(UserService userService, JdbcTemplate jdbc, SupabaseStorageService storage, NotificationStoreService notificationStoreService) {
        this.userService = userService;
        this.jdbc = jdbc;
        this.storage = storage;
        this.notificationStoreService = notificationStoreService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> createUser(@RequestBody UserRequestBody user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.created(URI.create("/users/" + createdUser.getId())).build();
    }

    @PostMapping("/admin/register")
    public ResponseEntity<User> createAdmin(@RequestBody UserRequestBody user) {
        User createdAdmin = userService.createAdmin(user);
        return ResponseEntity.created(URI.create("/users/" + createdAdmin.getId())).build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@RequestBody UserUpdateBody user, @PathVariable Long id, Authentication authentication) {
        if (!Long.valueOf(authentication.getName()).equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update your own account");
        }
        User updatedUser = userService.updateUser(user, id);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        if (!Long.valueOf(authentication.getName()).equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own account");
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseBody> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(toUserResponseBody(user));
    }
    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<UserResponseBody> getUserByNickname(@PathVariable String nickname) {
        User user = userService.getUserByNickname(nickname);
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
    @PostMapping("/fire")
    public ResponseEntity<Void> updateToken(Authentication authentication, @RequestBody FirebaseToken token) {
        Long userId = Long.valueOf(authentication.getName());
        userService.updateFireToken(userId, token.getToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping(
            value = "/{userId}/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public PhotoResponseBody uploadPhoto(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }
        if (!Long.valueOf(authentication.getName()).equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only upload your own profile picture"
            );
        }

        User user = userService.getUserById(userId);

        Long currentUserId;
        try {
            currentUserId = Long.valueOf(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Authenticated user ID unavailable"
            );
        }

        if (user.getProfilePicturePath() != null) {
            storage.delete(user.getProfilePicturePath());
        }

        String path = storage.upload("profiles", userId, file);

        try {
            int updated = jdbc.update("""
                    UPDATE public.users
                    SET profile_image_path = ?
                    WHERE id = ?
                    """, path, currentUserId);

            if (updated != 1) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Profile changed during upload"
                );
            }
        } catch (RuntimeException ex) {
            try {
                storage.delete(path);
            } catch (RuntimeException cleanupError) {
                ex.addSuppressed(cleanupError);
            }
            throw ex;
        }

        return new PhotoResponseBody(path);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId, Authentication authentication) {
        notificationStoreService.deleteNotification(Long.valueOf(authentication.getName()), notificationId);
        return ResponseEntity.noContent().build();
    }

    private UserResponseBody toUserResponseBody(User user) {
        String imageUrl = user.getProfilePicturePath() == null
                ? null
                : storage.createSignedUrl(user.getProfilePicturePath());
        return new UserResponseBody(
            user.getNickname(),
            user.isActive(),
            user.isVerified(),
            user.getEmail(),
            user.getRole().name(),
                user.getBio() == null ? "" : user.getBio(),
            imageUrl
        );
    }
}
