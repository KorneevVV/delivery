package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {

    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final DomainEventPublisher domainEventPublisher;

    public CompleteOrderCommandHandlerImpl(CourierRepository courierRepository, OrderRepository orderRepository,
            DomainEventPublisher domainEventPublisher) {
        this.courierRepository = courierRepository;
        this.orderRepository = orderRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var courierResult = courierRepository.getById(command.getCourierId());
        if (courierResult.isFailure()) {
            return UnitResult.failure(courierResult.getError());
        }

        var orderResult = orderRepository.getById(command.getOrderId());
        if (orderResult.isFailure()) {
            return UnitResult.failure(orderResult.getError());
        }

        var order = orderResult.getValue();
        if (order.getStatus() != OrderStatus.ASSIGNED) {
            return UnitResult.failure(Order.Errors.orderCannotBeCompleted());
        }

        var courier = courierResult.getValue();
        var completeAssignmentResult = courier.completeAssignment(command.getOrderId());
        if (completeAssignmentResult.isFailure()) {
            return completeAssignmentResult;
        }

        var completeOrderResult = order.complete();
        if (completeOrderResult.isFailure()) {
            return completeOrderResult;
        }

        courierRepository.update(courier);
        orderRepository.update(order);
        domainEventPublisher.publish(List.of(courier, order));

        return UnitResult.success();
    }
}
