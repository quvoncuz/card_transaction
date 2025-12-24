package card.uz.repository;

import card.uz.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, String> {
}
