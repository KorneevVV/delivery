package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {

    private static final int MIN_LOCATION_COORDINATE = 1;
    private static final int MAX_LOCATION_COORDINATE = 10;

    private final CourierRepository courierRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CreateCourierCommandHandlerImpl(CourierRepository courierRepository,
            DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateCourierCommand command) {
        var location = randomLocation();
        var courierResult = Courier.create(command.getName(), location);
        if (courierResult.isFailure()) {
            return Result.failure(courierResult.getError());
        }

        var courier = courierResult.getValue();
        courierRepository.add(courier);
        domainEventPublisher.publish(List.of(courier));

        return Result.success(courier.getId());
    }

    private Location randomLocation() {
        var random = ThreadLocalRandom.current();
        var x = random.nextInt(MIN_LOCATION_COORDINATE, MAX_LOCATION_COORDINATE + 1);
        var y = random.nextInt(MIN_LOCATION_COORDINATE, MAX_LOCATION_COORDINATE + 1);
        return Location.create(x, y).getValueOrThrow();
    }
}
