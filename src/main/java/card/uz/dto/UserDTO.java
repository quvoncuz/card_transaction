package card.uz.dto;

import card.uz.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long userId;
    private Role role;
    private String jwt;
}
