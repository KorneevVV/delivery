package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.domain.model.shared.Volume;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    /**
     * Сценарий: успешное создание Order с валидными параметрами.
     * <p>
     * Given: переданы Id, Location и Volume.
     * When: вызывается фабричный метод Order.create.
     * Then: возвращается успешный результат, а Order создается в статусе CREATED.
     */
    @Test
    @DisplayName("Успешное создание Order с валидными параметрами")
    void shouldBeCreatedWhenCreatedWithValidParams() {
        var id = UUID.randomUUID();
        var location = Location.create(5, 5).getValue();
        var volume = Volume.create(3).getValue();

        var result = Order.create(id, location, volume);

        assertThat(result.isSuccess()).isTrue();
        var order = result.getValue();
        assertThat(order.getId()).isEqualTo(id);
        assertThat(order.getLocation()).isEqualTo(location);
        assertThat(order.getVolume()).isEqualTo(volume);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    /**
     * Сценарий: ошибка создания Order, если Id пустой.
     * <p>
     * Given: передан пустой UUID в качестве Id, валидные Location и Volume.
     * When: вызывается фабричный метод Order.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Order, если Id пустой")
    void shouldRejectEmptyId() {
        var location = Location.create(5, 5).getValue();
        var volume = Volume.create(3).getValue();

        var result = Order.create(new UUID(0L, 0L), location, volume);

        assertThat(result.isFailure()).isTrue();
    }

    /**
     * Сценарий: ошибка создания Order, если Location не передан.
     * <p>
     * Given: передан null вместо Location и валидный Volume.
     * When: вызывается фабричный метод Order.create.
     * Then: выбрасывается NullPointerException.
     */
    @Test
    @DisplayName("Ошибка создания Order, если Location не передан")
    void shouldThrowWhenLocationIsNull() {
        var id = UUID.randomUUID();
        var volume = Volume.create(3).getValue();

        var exception = assertThrows(NullPointerException.class, () -> Order.create(id, null, volume));

        assertThat(exception).hasMessage("location");
    }

    /**
     * Сценарий: ошибка создания Order, если Volume не передан.
     * <p>
     * Given: передан null вместо Volume и валидный Location.
     * When: вызывается фабричный метод Order.create.
     * Then: выбрасывается NullPointerException.
     */
    @Test
    @DisplayName("Ошибка создания Order, если Volume не передан")
    void shouldThrowWhenVolumeIsNull() {
        var id = UUID.randomUUID();
        var location = Location.create(5, 5).getValue();

        var exception = assertThrows(NullPointerException.class, () -> Order.create(id, location, null));

        assertThat(exception).hasMessage("volume");
    }

    /**
     * Сценарий: Order можно назначить только из статуса Created.
     * <p>
     * Given: есть Order в статусе CREATED.
     * When: метод assign вызывается дважды.
     * Then: первый вызов успешен, второй возвращает ошибку, статус остается ASSIGNED.
     */
    @Test
    @DisplayName("Order можно назначить только из статуса Created")
    void shouldAssignOnlyWhenCreated() {
        var order = createOrder();

        var firstResult = order.assign();
        var secondResult = order.assign();

        assertThat(firstResult.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(secondResult.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    /**
     * Сценарий: Order можно завершить только из статуса Assigned.
     * <p>
     * Given: есть Order в статусе CREATED.
     * When: complete вызывается до назначения, после назначения и повторно после завершения.
     * Then: успешен только вызов после назначения, итоговый статус COMPLETED.
     */
    @Test
    @DisplayName("Order можно завершить только из статуса Assigned")
    void shouldCompleteOnlyWhenAssigned() {
        var order = createOrder();

        var completeBeforeAssignResult = order.complete();
        order.assign();
        var completeAfterAssignResult = order.complete();
        var completeAgainResult = order.complete();

        assertThat(completeBeforeAssignResult.isFailure()).isTrue();
        assertThat(completeAfterAssignResult.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(completeAgainResult.isFailure()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    private Order createOrder() {
        return Order.create(
                UUID.randomUUID(),
                Location.create(5, 5).getValue(),
                Volume.create(3).getValue()).getValue();
    }
}
