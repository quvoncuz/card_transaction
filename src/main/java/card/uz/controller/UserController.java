package card.uz.controller;

import card.uz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login/{userId}/{password}")
    public ResponseEntity<?> login(@PathVariable Long userId,
                                   @PathVariable String password) {
        return ResponseEntity.ok(userService.login(userId, password));
    }
}
