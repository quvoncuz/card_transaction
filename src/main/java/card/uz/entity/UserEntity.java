package card.uz.entity;

import card.uz.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "roles", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role roles;

    @Column
    private String password;

}
