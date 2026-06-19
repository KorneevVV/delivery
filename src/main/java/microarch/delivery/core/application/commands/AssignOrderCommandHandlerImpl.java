package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.services.DispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final DispatchService dispatchService;
    private final DomainEventPublisher domainEventPublisher;

    public AssignOrderCommandHandlerImpl(OrderRepository orderRepository, CourierRepository courierRepository,
            DispatchService dispatchService, DomainEventPublisher domainEventPublisher) {
        this.orderRepository = orderRepository;
        this.courierRepository = courierRepository;
        this.dispatchService = dispatchService;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {
        var orderResult = orderRepository.getAnyCreated();
        if (orderResult.isFailure()) {
            return UnitResult.failure(orderResult.getError());
        }

        var order = orderResult.getValue();
        var couriers = courierRepository.getAll();
        var dispatchResult = dispatchService.dispatch(order, couriers);
        if (dispatchResult.isFailure()) {
            return UnitResult.failure(dispatchResult.getError());
        }

        var courier = dispatchResult.getValue();
        orderRepository.update(order);
        courierRepository.update(courier);
        domainEventPublisher.publish(List.of(order, courier));

        return UnitResult.success();
    }
}
