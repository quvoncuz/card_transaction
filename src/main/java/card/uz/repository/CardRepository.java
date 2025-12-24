package card.uz.repository;

import card.uz.entity.CardEntity;
import card.uz.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotBlank;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, String> {
    long countByUserIdAndStatusIsNot(Long userId, Status status);
}
