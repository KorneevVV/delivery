package microarch.delivery.adapters.out.postgres.order;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository repository;

    public OrderRepositoryImpl(OrderJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order add(Order order) {
        return repository.save(order);
    }

    @Override
    public Order update(Order order) {
        return repository.save(order);
    }

    @Override
    public Result<Order, Error> getById(UUID id) {
        return repository.findById(id)
                .map(order -> Result.success(order))
                .orElseGet(() -> Result.failure(GeneralErrors.notFound("order", id)));
    }

    @Override
    public Result<Order, Error> getAnyCreated() {
        return repository.findFirstByStatus(OrderStatus.CREATED)
                .map(order -> Result.success(order))
                .orElseGet(() -> Result.failure(GeneralErrors.notFound("order", "status=CREATED")));
    }

    @Override
    public List<Order> getAllAssigned() {
        return repository.findAllByStatus(OrderStatus.ASSIGNED);
    }
}
