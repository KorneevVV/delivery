package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private static final int MIN_LOCATION_COORDINATE = 1;
    private static final int MAX_LOCATION_COORDINATE = 10;

    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CreateOrderCommandHandlerImpl(OrderRepository orderRepository, DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateOrderCommand command) {
        var location = randomLocation();
        var orderResult = Order.create(command.getOrderId(), location, command.getVolume());
        if (orderResult.isFailure()) {
            return Result.failure(orderResult.getError());
        }

        var order = orderResult.getValue();
        orderRepository.add(order);
        domainEventPublisher.publish(List.of(order));

        return Result.success(order.getId());
    }

    private Location randomLocation() {
        var random = ThreadLocalRandom.current();
        var x = random.nextInt(MIN_LOCATION_COORDINATE, MAX_LOCATION_COORDINATE + 1);
        var y = random.nextInt(MIN_LOCATION_COORDINATE, MAX_LOCATION_COORDINATE + 1);
        return Location.create(x, y).getValueOrThrow();
    }
}
