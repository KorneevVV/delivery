package microarch.delivery.core.domain.services;

import libs.errs.GeneralErrors;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.domain.model.shared.Volume;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DispatchServiceTest {

    private final DispatchService dispatchService = new DispatchService();

    /**
     * Сценарий: успешная диспетчеризация назначает заказ ближайшему доступному Courier.
     * <p>
     * Given: есть Order в статусе CREATED и доступный Courier.
     * When: вызывается DispatchService.dispatch.
     * Then: возвращается выбранный Courier, Order переходит в ASSIGNED, а Courier получает Assignment.
     */
    @Test
    @DisplayName("Успешная диспетчеризация назначает заказ курьеру")
    void shouldDispatchOrderToCourier() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);
        var courier = createCourierAt(Location.create(5, 6).getValue());

        var result = dispatchService.dispatch(order, List.of(courier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isSameAs(courier);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(courier.getAssignments()).hasSize(1);
        var assignment = courier.getAssignments().getFirst();
        assertThat(assignment.getOrderId()).isEqualTo(order.getId());
        assertThat(assignment.getVolume()).isEqualTo(order.getVolume());
        assertThat(assignment.getLocation()).isEqualTo(order.getLocation());
    }

    /**
     * Сценарий: выбирается ближайший доступный Courier.
     * <p>
     * Given: есть два доступных Courier на разной дистанции от Order.
     * When: выполняется dispatch.
     * Then: выбирается Courier с минимальной дистанцией.
     */
    @Test
    @DisplayName("Выбирается ближайший курьер")
    void shouldChooseNearestCourier() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);
        var farCourier = createCourierAt(Location.create(1, 1).getValue());
        var nearCourier = createCourierAt(Location.create(5, 6).getValue());

        var result = dispatchService.dispatch(order, List.of(farCourier, nearCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isSameAs(nearCourier);
        assertThat(nearCourier.getAssignments()).hasSize(1);
        assertThat(farCourier.getAssignments()).isEmpty();
    }

    /**
     * Сценарий: при одинаковой дистанции выбирается первый Courier из входного списка.
     * <p>
     * Given: есть два Courier на одинаковой дистанции.
     * When: выполняется dispatch.
     * Then: выбирается первый Courier из списка.
     */
    @Test
    @DisplayName("При одинаковой дистанции выбирается первый курьер")
    void shouldChooseFirstCourierWhenDistanceIsEqual() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);
        var firstCourier = createCourierAt(Location.create(4, 5).getValue());
        var secondCourier = createCourierAt(Location.create(5, 6).getValue());

        var result = dispatchService.dispatch(order, List.of(firstCourier, secondCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isSameAs(firstCourier);
        assertThat(firstCourier.getAssignments()).hasSize(1);
        assertThat(secondCourier.getAssignments()).isEmpty();
    }

    /**
     * Сценарий: переполненный Courier пропускается, даже если он ближе.
     * <p>
     * Given: ближайший Courier не имеет свободного объема, а дальний Courier доступен.
     * When: выполняется dispatch.
     * Then: заказ назначается дальнему доступному Courier.
     */
    @Test
    @DisplayName("Переполненный курьер пропускается")
    void shouldSkipCourierWithoutCapacity() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);
        var fullCourier = createCourierAt(Location.create(5, 6).getValue());
        fullCourier.assign(UUID.randomUUID(), Volume.create(20).getValue(), Location.create(5, 7).getValue());
        var availableCourier = createCourierAt(Location.create(1, 1).getValue());

        var result = dispatchService.dispatch(order, List.of(fullCourier, availableCourier));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isSameAs(availableCourier);
        assertThat(fullCourier.getAssignments()).hasSize(1);
        assertThat(availableCourier.getAssignments()).hasSize(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    /**
     * Сценарий: если все Courier переполнены, возвращается ошибка.
     * <p>
     * Given: все Courier не могут взять объем Order.
     * When: выполняется dispatch.
     * Then: возвращается ошибка, Order остается CREATED, новые Assignment не добавляются.
     */
    @Test
    @DisplayName("Если все курьеры переполнены, возвращается ошибка")
    void shouldFailWhenAllCouriersAreFull() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);
        var firstCourier = createFullCourierAt(Location.create(5, 6).getValue());
        var secondCourier = createFullCourierAt(Location.create(1, 1).getValue());

        var result = dispatchService.dispatch(order, List.of(firstCourier, secondCourier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(DispatchService.Errors.courierNotFound());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(firstCourier.getAssignments()).hasSize(1);
        assertThat(secondCourier.getAssignments()).hasSize(1);
    }

    /**
     * Сценарий: пустой или null список Courier считается ошибкой входных данных.
     * <p>
     * Given: есть Order и нет списка Courier.
     * When: выполняется dispatch.
     * Then: возвращается бизнес-ошибка обязательного значения.
     */
    @Test
    @DisplayName("Пустой или null список курьеров возвращает ошибку")
    void shouldFailWhenCourierListIsNullOrEmpty() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);

        var nullResult = dispatchService.dispatch(order, null);
        var emptyResult = dispatchService.dispatch(order, List.of());

        assertThat(nullResult.isFailure()).isTrue();
        assertThat(nullResult.getError()).isEqualTo(GeneralErrors.valueIsRequired("couriers"));
        assertThat(emptyResult.isFailure()).isTrue();
        assertThat(emptyResult.getError()).isEqualTo(GeneralErrors.valueIsRequired("couriers"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    /**
     * Сценарий: null в списке Courier считается ошибкой входных данных.
     * <p>
     * Given: список Courier содержит null.
     * When: выполняется dispatch.
     * Then: возвращается бизнес-ошибка обязательного значения.
     */
    @Test
    @DisplayName("Null в списке курьеров возвращает ошибку")
    void shouldFailWhenCourierListContainsNull() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);
        var courier = createCourierAt(Location.create(5, 6).getValue());

        var result = dispatchService.dispatch(order, Arrays.asList(courier, null));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("courier"));
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(courier.getAssignments()).isEmpty();
    }

    /**
     * Сценарий: заказ не в статусе CREATED не диспетчеризуется.
     * <p>
     * Given: есть Order в статусе ASSIGNED и доступный Courier.
     * When: выполняется dispatch.
     * Then: возвращается ошибка, а Courier не меняется.
     */
    @Test
    @DisplayName("Заказ не в CREATED возвращает ошибку")
    void shouldFailWhenOrderIsNotCreated() {
        var order = createOrderAt(Location.create(5, 5).getValue(), 3);
        order.assign();
        var courier = createCourierAt(Location.create(5, 6).getValue());

        var result = dispatchService.dispatch(order, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(DispatchService.Errors.orderMustBeCreated());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(courier.getAssignments()).isEmpty();
    }

    /**
     * Сценарий: null Order считается ошибкой входных данных.
     * <p>
     * Given: вместо Order передан null.
     * When: выполняется dispatch.
     * Then: возвращается бизнес-ошибка обязательного значения.
     */
    @Test
    @DisplayName("Null заказ возвращает ошибку")
    void shouldFailWhenOrderIsNull() {
        var courier = createCourierAt(Location.create(5, 6).getValue());

        var result = dispatchService.dispatch(null, List.of(courier));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(GeneralErrors.valueIsRequired("order"));
        assertThat(courier.getAssignments()).isEmpty();
    }

    /**
     * Сценарий: DispatchService создается как Spring bean без поднятия всего приложения.
     * <p>
     * Given: Spring context сканирует только пакет domain services.
     * When: context запускается.
     * Then: DispatchService доступен как bean.
     */
    @Test
    @DisplayName("DispatchService создается как Spring bean")
    void shouldCreateDispatchServiceBean() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.scan("microarch.delivery.core.domain.services");
            context.refresh();

            assertThat(context.getBean(DispatchService.class)).isNotNull();
        }
    }

    private Order createOrderAt(Location location, int volume) {
        return Order.create(UUID.randomUUID(), location, Volume.create(volume).getValue()).getValue();
    }

    private Courier createCourierAt(Location location) {
        return Courier.create("Ivan", location).getValue();
    }

    private Courier createFullCourierAt(Location location) {
        var courier = createCourierAt(location);
        courier.assign(UUID.randomUUID(), Volume.create(20).getValue(), Location.create(10, 10).getValue());
        return courier;
    }
}
