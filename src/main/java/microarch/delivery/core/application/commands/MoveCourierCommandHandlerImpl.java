package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MoveCourierCommandHandlerImpl implements MoveCourierCommandHandler {

    private final CourierRepository courierRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MoveCourierCommandHandlerImpl(CourierRepository courierRepository,
            DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(MoveCourierCommand command) {
        var courierResult = courierRepository.getById(command.getCourierId());
        if (courierResult.isFailure()) {
            return UnitResult.failure(courierResult.getError());
        }

        var courier = courierResult.getValue();
        courier.moveTo(command.getLocation());

        courierRepository.update(courier);
        domainEventPublisher.publish(List.of(courier));

        return UnitResult.success();
    }
}
