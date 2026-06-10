package microarch.delivery.core.domain.model.courier;

import microarch.delivery.core.domain.model.shared.Volume;
import microarch.delivery.core.domain.model.shared.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CourierTest {

    /**
     * Сценарий: успешное создание Courier с валидными параметрами.
     * <p>
     * Given: переданы имя курьера и Location.
     * When: вызывается фабричный метод Courier.create.
     * Then: возвращается успешный результат, а Courier создается с MaxVolume = 20 и пустым списком Assignment.
     */
    @Test
    @DisplayName("Успешное создание Courier с валидными параметрами")
    void shouldCreateCourierWithMaxVolume() {
        var location = Location.create(5, 5).getValue();

        var result = Courier.create("Ivan", location);

        assertThat(result.isSuccess()).isTrue();
        var courier = result.getValue();
        assertThat(courier.getId()).isNotNull();
        assertThat(courier.getName()).isEqualTo("Ivan");
        assertThat(courier.getLocation()).isEqualTo(location);
        assertThat(courier.getMaxVolume()).isEqualTo(Volume.create(20).getValue());
        assertThat(courier.getAssignments()).isEmpty();
    }

    /**
     * Сценарий: ошибка создания Courier, если имя пустое.
     * <p>
     * Given: передано пустое имя и валидный Location.
     * When: вызывается фабричный метод Courier.create.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Ошибка создания Courier, если имя пустое")
    void shouldRejectBlankName() {
        var location = Location.create(5, 5).getValue();

        var result = Courier.create(" ", location);

        assertThat(result.isFailure()).isTrue();
    }

    /**
     * Сценарий: ошибка создания Courier, если Location не передан.
     * <p>
     * Given: передан null вместо Location.
     * When: вызывается фабричный метод Courier.create.
     * Then: выбрасывается NullPointerException.
     */
    @Test
    @DisplayName("Ошибка создания Courier, если Location не передан")
    void shouldThrowWhenLocationIsNull() {
        assertThrows(NullPointerException.class, () -> Courier.create("Ivan", null));
    }

    /**
     * Сценарий: Courier может взять заказ, если активный объем не превышает максимум.
     * <p>
     * Given: есть Courier без назначений.
     * When: курьеру назначаются заказы объемом 12 и 8 литров.
     * Then: оба назначения успешны, а взять еще 1 литр курьер уже не может.
     */
    @Test
    @DisplayName("Courier может взять заказ, если активный объем не превышает максимум")
    void shouldTakeOrderWhenActiveVolumeDoesNotExceedMax() {
        var courier = createCourierAt(Location.create(5, 5).getValue());

        var firstResult = courier.assign(
                UUID.randomUUID(),
                Volume.create(12).getValue(),
                Location.create(6, 5).getValue());
        var secondResult = courier.assign(
                UUID.randomUUID(),
                Volume.create(8).getValue(),
                Location.create(7, 5).getValue());

        assertThat(firstResult.isSuccess()).isTrue();
        assertThat(secondResult.isSuccess()).isTrue();
        assertThat(courier.canTake(Volume.create(1).getValue())).isFalse();
        assertThat(courier.getAssignments()).hasSize(2);
    }

    /**
     * Сценарий: Courier не может взять заказ, если активный объем превысит максимум.
     * <p>
     * Given: есть Courier с активным Assignment объемом 15 литров.
     * When: курьеру назначается еще один заказ объемом 6 литров.
     * Then: возвращается результат с ошибкой, новый Assignment не добавляется.
     */
    @Test
    @DisplayName("Courier не может взять заказ, если активный объем превысит максимум")
    void shouldRejectOrderWhenActiveVolumeExceedsMax() {
        var courier = createCourierAt(Location.create(5, 5).getValue());
        courier.assign(
                UUID.randomUUID(),
                Volume.create(15).getValue(),
                Location.create(6, 5).getValue());

        var result = courier.assign(
                UUID.randomUUID(),
                Volume.create(6).getValue(),
                Location.create(7, 5).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
    }

    /**
     * Сценарий: Courier освобождает объем после завершения Assignment.
     * <p>
     * Given: есть Courier с активным Assignment на весь доступный объем.
     * When: Assignment успешно завершается.
     * Then: завершенный Assignment больше не учитывается в активном объеме курьера.
     */
    @Test
    @DisplayName("Courier освобождает объем после завершения Assignment")
    void shouldFreeCapacityAfterAssignmentCompleted() {
        var courier = createCourierAt(Location.create(5, 5).getValue());
        var firstOrderId = UUID.randomUUID();
        courier.assign(firstOrderId, Volume.create(20).getValue(), Location.create(5, 6).getValue());

        var completeResult = courier.completeAssignment(firstOrderId);

        assertThat(completeResult.isSuccess()).isTrue();
        assertThat(courier.canTake(Volume.create(20).getValue())).isTrue();
    }

    /**
     * Сценарий: Courier создает Assignment в статусе Assigned при назначении заказа.
     * <p>
     * Given: есть Courier без назначений и данные заказа.
     * When: вызывается метод assign.
     * Then: создается Assignment с OrderId, Volume, Location заказа и статусом ASSIGNED.
     */
    @Test
    @DisplayName("Courier создает Assignment в статусе Assigned при назначении заказа")
    void shouldCreateAssignedAssignmentWhenOrderAssigned() {
        var courier = createCourierAt(Location.create(5, 5).getValue());
        var orderId = UUID.randomUUID();
        var volume = Volume.create(3).getValue();
        var orderLocation = Location.create(6, 5).getValue();

        var result = courier.assign(orderId, volume, orderLocation);

        assertThat(result.isSuccess()).isTrue();
        var assignment = result.getValue();
        assertThat(assignment.getOrderId()).isEqualTo(orderId);
        assertThat(assignment.getVolume()).isEqualTo(volume);
        assertThat(assignment.getLocation()).isEqualTo(orderLocation);
        assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
        assertThat(courier.getAssignments()).containsExactly(assignment);
    }

    /**
     * Сценарий: Courier не может создать два Assignment для одного OrderId.
     * <p>
     * Given: есть Courier с Assignment для OrderId.
     * When: курьеру повторно назначается заказ с тем же OrderId.
     * Then: возвращается результат с ошибкой, повторный Assignment не добавляется.
     */
    @Test
    @DisplayName("Courier не может создать два Assignment для одного OrderId")
    void shouldRejectDuplicateOrderAssignment() {
        var courier = createCourierAt(Location.create(5, 5).getValue());
        var orderId = UUID.randomUUID();
        courier.assign(orderId, Volume.create(3).getValue(), Location.create(6, 5).getValue());

        var result = courier.assign(orderId, Volume.create(3).getValue(), Location.create(7, 5).getValue());

        assertThat(result.isFailure()).isTrue();
        assertThat(courier.getAssignments()).hasSize(1);
    }

    /**
     * Сценарий: Courier завершает Assignment, если находится в одной клетке от заказа.
     * <p>
     * Given: есть Courier и Assignment с Location заказа на расстоянии одной клетки.
     * When: вызывается метод completeAssignment.
     * Then: возвращается успешный результат, а Assignment переходит в статус COMPLETED.
     */
    @Test
    @DisplayName("Courier завершает Assignment, если находится в одной клетке от заказа")
    void shouldCompleteAssignmentWhenCourierIsOneCellAway() {
        var courier = createCourierAt(Location.create(5, 5).getValue());
        var orderId = UUID.randomUUID();
        courier.assign(orderId, Volume.create(3).getValue(), Location.create(5, 6).getValue());

        var result = courier.completeAssignment(orderId);

        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.COMPLETED);
    }

    /**
     * Сценарий: Courier не завершает Assignment, если находится дальше одной клетки от заказа.
     * <p>
     * Given: есть Courier и Assignment с Location заказа дальше одной клетки.
     * When: вызывается метод completeAssignment.
     * Then: возвращается результат с ошибкой, Assignment остается в статусе ASSIGNED.
     */
    @Test
    @DisplayName("Courier не завершает Assignment, если находится дальше одной клетки от заказа")
    void shouldNotCompleteAssignmentWhenCourierIsTooFar() {
        var courier = createCourierAt(Location.create(5, 5).getValue());
        var orderId = UUID.randomUUID();
        courier.assign(orderId, Volume.create(3).getValue(), Location.create(5, 7).getValue());

        var result = courier.completeAssignment(orderId);

        assertThat(result.isFailure()).isTrue();
        assertThat(courier.getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    /**
     * Сценарий: Courier возвращает ошибку, если Assignment не найден.
     * <p>
     * Given: есть Courier без Assignment для переданного OrderId.
     * When: вызывается метод completeAssignment.
     * Then: возвращается результат с ошибкой.
     */
    @Test
    @DisplayName("Courier возвращает ошибку, если Assignment не найден")
    void shouldFailWhenAssignmentNotFound() {
        var courier = createCourierAt(Location.create(5, 5).getValue());

        var result = courier.completeAssignment(UUID.randomUUID());

        assertThat(result.isFailure()).isTrue();
    }

    /**
     * Сценарий: Courier может изменить Location.
     * <p>
     * Given: есть Courier с текущим Location.
     * When: вызывается метод moveTo с новым Location.
     * Then: Location курьера меняется на переданный.
     */
    @Test
    @DisplayName("Courier может изменить Location")
    void shouldMoveToNewLocation() {
        var courier = createCourierAt(Location.create(5, 5).getValue());
        var newLocation = Location.create(7, 8).getValue();

        courier.moveTo(newLocation);

        assertThat(courier.getLocation()).isEqualTo(newLocation);
    }

    /**
     * Сценарий: ошибка перемещения Courier, если Location не передан.
     * <p>
     * Given: есть Courier.
     * When: метод moveTo вызывается с null вместо Location.
     * Then: выбрасывается NullPointerException.
     */
    @Test
    @DisplayName("Ошибка перемещения Courier, если Location не передан")
    void shouldThrowWhenMovingToNullLocation() {
        var courier = createCourierAt(Location.create(5, 5).getValue());

        assertThrows(NullPointerException.class, () -> courier.moveTo(null));
    }

    private Courier createCourierAt(Location location) {
        return Courier.create("Ivan", location).getValue();
    }
}
