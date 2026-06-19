package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.domain.model.shared.Volume;
import microarch.delivery.core.domain.services.DispatchService;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssignOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    /**
     * Сценарий: AssignOrderCommandHandler назначает созданный заказ ближайшему Courier.
     * <p>
     * Given: есть Order в статусе CREATED и два доступных Courier на разной дистанции от заказа.
     * When: вызывается AssignOrderCommandHandler.handle.
     * Then: заказ переходит в ASSIGNED, ближайший Courier получает Assignment, оба измененных агрегата сохраняются.
     */
    @Test
    @DisplayName("AssignOrderCommandHandler назначает заказ ближайшему курьеру")
    void AssignOrderCommandHandler_ShouldAssignNearestCourier_WhenCreatedOrderExists() {
        var order = createOrderAt(Location.create(5, 5).getValue());
        var farCourier = createCourierAt("Far", Location.create(1, 1).getValue());
        var nearestCourier = createCourierAt("Nearest", Location.create(5, 6).getValue());
        var handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, new DispatchService(),
                domainEventPublisher);
        var command = AssignOrderCommand.create().getValue();

        when(orderRepository.getAnyCreated()).thenReturn(Result.success(order));
        when(courierRepository.getAll()).thenReturn(List.of(farCourier, nearestCourier));

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(nearestCourier.getAssignments()).hasSize(1);
        assertThat(nearestCourier.getAssignments().getFirst().getOrderId()).isEqualTo(order.getId());
        assertThat(farCourier.getAssignments()).isEmpty();
        verify(orderRepository).update(order);
        verify(courierRepository).update(nearestCourier);
        verify(courierRepository, never()).update(farCourier);
    }

    private Order createOrderAt(Location location) {
        return Order.create(UUID.randomUUID(), location, Volume.create(3).getValue()).getValue();
    }

    private Courier createCourierAt(String name, Location location) {
        return Courier.create(name, location).getValue();
    }
}
