package card.uz.service;

import card.uz.dto.UserDTO;
import card.uz.entity.UserEntity;
import card.uz.exp.AppException;
import card.uz.repository.UserRepository;
import card.uz.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserDTO login(Long userId, String password) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new AppException("User not found"));

        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new AppException("Invalid credentials");
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(user.getId());
        userDTO.setRole(user.getRoles());
        userDTO.setJwt(JwtUtil.encode(userId, user.getRoles().toString()));
        return userDTO;
    }

//    public UserDTO
}
