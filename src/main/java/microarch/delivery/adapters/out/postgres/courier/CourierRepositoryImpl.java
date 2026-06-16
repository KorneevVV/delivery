package microarch.delivery.adapters.out.postgres.courier;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository repository;

    public CourierRepositoryImpl(CourierJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Courier add(Courier courier) {
        return repository.save(courier);
    }

    @Override
    public Courier update(Courier courier) {
        return repository.save(courier);
    }

    @Override
    public Result<Courier, Error> getById(UUID id) {
        return repository.findById(id)
                .map(courier -> Result.success(courier))
                .orElseGet(() -> Result.failure(GeneralErrors.notFound("courier", id)));
    }

    @Override
    public List<Courier> getAll() {
        return repository.findAll();
    }
}
