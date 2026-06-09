package microarch.delivery.core.domain.model.courier;

import microarch.delivery.core.domain.model.order.Volume;
import microarch.delivery.core.domain.model.shared.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssignmentTest {

    /**
     * Сценарий: Assignment наследуется от BaseEntity.
     * <p>
     * Given: есть класс Assignment.
     * When: проверяется его базовый класс.
     * Then: Assignment является Entity и наследуется от BaseEntity.
     */
    @Test
    @DisplayName("Assignment наследуется от BaseEntity")
    void derivedFromBaseEntity() {
        assertThat(Assignment.class.getSuperclass().getSimpleName()).isEqualTo("BaseEntity");
    }

    /**
     * Сценарий: успешное создание Assignment с валидными параметрами.
     * <p>
     * Given: переданы OrderId, Volume и Location.
     * When: вызывается фабричный метод Assignment.create.
     * Then: возвращается успешный результат, а Assignment создается в статусе ASSIGNED.
     */
    @Test
    @DisplayName("Успешное создание Assignment с валидными параметрами")
    void shouldBeAssignedWhenCreatedWithValidParams() {
        var orderId = UUID.randomUUID();
        var volume = Volume.create(3).getValue();
        var location = Location.create(5, 5).getValue();

        var result = Assignment.create(orderId, volume, location);

        assertThat(result.isSuccess()).isTrue();
        var assignment = result.getValue();
        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getOrderId()).isEqualTo(orderId);
        assertThat(assignment.getVolume()).isEqualTo(volume);
        assertThat(assignment.getLocation()).isEqualTo(location);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    /**
     * Сценарий: ошибка создания Assignment, если OrderId пустой.
     * <p>
     * Given: передан пустой UUID в качестве OrderId, валидные Volume и Location.
     * When: вызывается фабричный метод Assignment.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Assignment, если OrderId пустой")
    void shouldRejectEmptyOrderId() {
        var volume = Volume.create(3).getValue();
        var location = Location.create(5, 5).getValue();

        var result = Assignment.create(new UUID(0L, 0L), volume, location);

        assertThat(result.isFailure()).isTrue();
    }

    /**
     * Сценарий: успешное завершение Assignment, если курьер находится в одной клетке от заказа.
     * <p>
     * Given: есть Assignment с Location x = 5, y = 5 и Location курьера x = 5, y = 6.
     * When: вызывается метод complete.
     * Then: возвращается успешный результат, а статус Assignment меняется на COMPLETED.
     */
    @Test
    @DisplayName("Успешное завершение Assignment, если курьер находится в одной клетке от заказа")
    void shouldCompleteWhenCourierIsOneCellAway() {
        var assignment = createAssignmentAt(Location.create(5, 5).getValue());
        var courierLocation = Location.create(5, 6).getValue();

        var result = assignment.complete(courierLocation);

        assertThat(result.isSuccess()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    /**
     * Сценарий: ошибка завершения Assignment, если курьер находится дальше одной клетки от заказа.
     * <p>
     * Given: есть Assignment с Location x = 5, y = 5 и Location курьера x = 5, y = 7.
     * When: вызывается метод complete.
     * Then: возвращается результат с ошибкой, а статус Assignment остается ASSIGNED.
     */
    @Test
    @DisplayName("Ошибка завершения Assignment, если курьер находится дальше одной клетки от заказа")
    void shouldNotCompleteWhenCourierIsMoreThanOneCellAway() {
        var assignment = createAssignmentAt(Location.create(5, 5).getValue());
        var courierLocation = Location.create(5, 7).getValue();

        var result = assignment.complete(courierLocation);

        assertThat(result.isFailure()).isTrue();
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    /**
     * Сценарий: две Assignment с одинаковым Id должны быть равны.
     * <p>
     * Given: восстановлены две Assignment с одинаковым Id и разными остальными полями.
     * When: выполняется сравнение объектов через equals.
     * Then: объекты считаются равными и имеют одинаковый hashCode.
     */
    @Test
    @DisplayName("Две Assignment с одинаковым Id должны быть равны")
    void shouldBeEqualWhenIdsAreEqual() {
        var id = UUID.randomUUID();
        var first = Assignment.restore(
                id,
                UUID.randomUUID(),
                Volume.create(1).getValue(),
                Location.create(1, 1).getValue(),
                AssignmentStatus.ASSIGNED);
        var second = Assignment.restore(
                id,
                UUID.randomUUID(),
                Volume.create(5).getValue(),
                Location.create(9, 9).getValue(),
                AssignmentStatus.COMPLETED);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    /**
     * Сценарий: ошибка завершения Assignment, если Location курьера не передан.
     * <p>
     * Given: есть Assignment в статусе ASSIGNED.
     * When: метод complete вызывается с null вместо Location курьера.
     * Then: выбрасывается NullPointerException.
     */
    @Test
    @DisplayName("Ошибка завершения Assignment, если Location курьера не передан")
    void shouldThrowWhenCompletingWithNullLocation() {
        var assignment = createAssignmentAt(Location.create(5, 5).getValue());

        assertThrows(NullPointerException.class, () -> assignment.complete(null));
    }

    private Assignment createAssignmentAt(Location location) {
        return Assignment.create(UUID.randomUUID(), Volume.create(1).getValue(), location).getValue();
    }
}
