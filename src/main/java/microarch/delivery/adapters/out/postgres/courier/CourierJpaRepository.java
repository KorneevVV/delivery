package microarch.delivery.adapters.out.postgres.courier;

import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourierJpaRepository extends JpaRepository<Courier, UUID> {

    @Override
    @EntityGraph(attributePaths = "assignments")
    Optional<Courier> findById(UUID uuid);

    @Override
    @EntityGraph(attributePaths = "assignments")
    List<Courier> findAll();
}
