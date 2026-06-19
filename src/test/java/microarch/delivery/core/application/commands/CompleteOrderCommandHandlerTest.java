package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.domain.model.shared.Volume;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteOrderCommandHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    /**
     * Сценарий: CompleteOrderCommandHandler завершает заказ, если Courier рядом с точкой доставки.
     * <p>
     * Given: есть Order в статусе ASSIGNED и Courier с Assignment для этого Order на расстоянии одной клетки.
     * When: вызывается CompleteOrderCommandHandler.handle.
     * Then: Assignment завершается и удаляется у Courier, Order переходит в COMPLETED, оба агрегата сохраняются.
     */
    @Test
    @DisplayName("CompleteOrderCommandHandler завершает заказ, если курьер рядом")
    void CompleteOrderCommandHandler_ShouldCompleteOrder_WhenCourierIsNearOrder() {
        var order = createOrderAt(Location.create(5, 5).getValue());
        var courier = Courier.create("Ivan", Location.create(5, 6).getValue()).getValue();
        courier.assign(order.getId(), order.getVolume(), order.getLocation());
        order.assign();
        var handler = new CompleteOrderCommandHandlerImpl(courierRepository, orderRepository, domainEventPublisher);
        var command = CompleteOrderCommand.create(courier.getId(), order.getId()).getValue();

        when(courierRepository.getById(courier.getId())).thenReturn(Result.success(courier));
        when(orderRepository.getById(order.getId())).thenReturn(Result.success(order));

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(courier.getAssignments()).isEmpty();
        verify(courierRepository).update(courier);
        verify(orderRepository).update(order);
    }

    private Order createOrderAt(Location location) {
        return Order.create(UUID.randomUUID(), location, Volume.create(3).getValue()).getValue();
    }
}
