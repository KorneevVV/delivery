package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final DomainEventPublisher domainEventPublisher = mock(DomainEventPublisher.class);

    /**
     * Сценарий: CreateOrderCommandHandler создает Order с валидными параметрами.
     * <p>
     * Given: передана команда создания заказа с валидным orderId, адресными полями и volume.
     * When: вызывается CreateOrderCommandHandler.handle.
     * Then: Order сохраняется в статусе CREATED, возвращается orderId, а Location находится в допустимых границах.
     */
    @Test
    @DisplayName("CreateOrderCommandHandler создает Order с валидными параметрами")
    void CreateOrderCommandHandler_ShouldCreateOrder_WhenParamsAreCorrect() {
        var orderId = UUID.randomUUID();
        var command = CreateOrderCommand.create(orderId, "Russia", "Moscow", "Tverskaya", "1", "2", 3).getValue();
        var handler = new CreateOrderCommandHandlerImpl(orderRepository, domainEventPublisher);
        when(orderRepository.add(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(orderId);

        var orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).add(orderCaptor.capture());
        var order = orderCaptor.getValue();
        assertThat(order.getId()).isEqualTo(orderId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getVolume().getValue()).isEqualTo(3);
        assertThat(order.getLocation().getX()).isBetween(1, 10);
        assertThat(order.getLocation().getY()).isBetween(1, 10);
    }
}
