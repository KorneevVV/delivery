package microarch.delivery.adapters.out.postgres;

import jakarta.persistence.EntityManager;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.courier.AssignmentStatus;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.domain.model.shared.Volume;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.transaction.TestTransaction;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void clearPersistenceContext() {
        entityManager.clear();
    }

    /**
     * Сценарий: Order можно сохранить и восстановить из базы.
     * <p>
     * Given: создан новый Order в статусе CREATED.
     * When: Order сохраняется через repository и затем читается по id.
     * Then: восстановленный объект совпадает по данным и статусу.
     */
    @Test
    @DisplayName("Order можно сохранить и восстановить из базы")
    void shouldSaveAndRestoreOrder() {
        var order = createOrder();

        orderRepository.add(order);
        entityManager.flush();
        entityManager.clear();

        var result = orderRepository.getById(order.getId());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getId()).isEqualTo(order.getId());
        assertThat(result.getValue().getLocation()).isEqualTo(order.getLocation());
        assertThat(result.getValue().getVolume()).isEqualTo(order.getVolume());
        assertThat(result.getValue().getStatus()).isEqualTo(order.getStatus());
    }

    /**
     * Сценарий: Order можно обновить и затем восстановить с новым статусом.
     * <p>
     * Given: сохранен Order в статусе CREATED.
     * When: Order переводится в ASSIGNED и обновляется через repository.
     * Then: при повторном чтении статус остается ASSIGNED.
     */
    @Test
    @DisplayName("Order можно обновить и восстановить с новым статусом")
    void shouldUpdateOrder() {
        var order = createOrder();

        orderRepository.add(order);
        order.assign();
        orderRepository.update(order);
        entityManager.flush();
        entityManager.clear();

        var result = orderRepository.getById(order.getId());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getStatus()).isEqualTo(OrderStatus.ASSIGNED);
    }

    /**
     * Сценарий: repository возвращает любой Order в статусе CREATED.
     * <p>
     * Given: сохранены два Order, один CREATED и один ASSIGNED.
     * When: вызывается getAnyCreated.
     * Then: возвращается Order в статусе CREATED.
     */
    @Test
    @DisplayName("Repository возвращает любой Order в статусе CREATED")
    void shouldReturnAnyCreatedOrder() {
        var createdOrder = createOrder();
        var assignedOrder = createOrder();
        assignedOrder.assign();

        orderRepository.add(createdOrder);
        orderRepository.add(assignedOrder);
        entityManager.flush();
        entityManager.clear();

        var result = orderRepository.getAnyCreated();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    /**
     * Сценарий: repository возвращает все незавершенные Order.
     * <p>
     * Given: сохранены Order в статусах CREATED, ASSIGNED и COMPLETED.
     * When: вызывается getAllNotCompleted.
     * Then: возвращаются только созданные и назначенные заказы.
     */
    @Test
    @DisplayName("Repository возвращает все незавершенные Order")
    void shouldReturnAllNotCompletedOrders() {
        var createdOrder = createOrder();
        var assignedOrder = createOrder();
        assignedOrder.assign();
        var completedOrder = createOrder();
        completedOrder.assign();
        completedOrder.complete();

        orderRepository.add(createdOrder);
        orderRepository.add(assignedOrder);
        orderRepository.add(completedOrder);
        entityManager.flush();
        entityManager.clear();

        var notCompletedOrders = orderRepository.getAllNotCompleted();

        assertThat(notCompletedOrders).hasSize(2);
        assertThat(notCompletedOrders)
                .extracting(Order::getStatus)
                .containsExactlyInAnyOrder(OrderStatus.CREATED, OrderStatus.ASSIGNED);
    }

    /**
     * Сценарий: Courier можно сохранить и восстановить вместе с назначениями.
     * <p>
     * Given: создан Courier и добавлен Assignment.
     * When: Courier сохраняется через repository и затем читается по id.
     * Then: восстановленный Courier содержит тот же Assignment.
     */
    @Test
    @DisplayName("Courier можно сохранить и восстановить вместе с назначениями")
    void shouldSaveAndRestoreCourierWithAssignments() {
        var courier = createCourier();
        var orderId = UUID.randomUUID();
        courier.assign(orderId, Volume.create(4).getValue(), Location.create(6, 5).getValue());

        courierRepository.add(courier);
        entityManager.flush();
        entityManager.clear();

        var result = courierRepository.getById(courier.getId());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getId()).isEqualTo(courier.getId());
        assertThat(result.getValue().getLocation()).isEqualTo(courier.getLocation());
        assertThat(result.getValue().getAssignments()).hasSize(1);
        assertThat(result.getValue().getAssignments().getFirst().getOrderId()).isEqualTo(orderId);
        assertThat(result.getValue().getAssignments().getFirst().getStatus()).isEqualTo(AssignmentStatus.ASSIGNED);
    }

    /**
     * Сценарий: Courier можно обновить и затем восстановить с новой локацией.
     * <p>
     * Given: сохранен Courier без назначений.
     * When: Courier перемещается в новую точку и обновляется через repository.
     * Then: при повторном чтении Location совпадает с новым значением.
     */
    @Test
    @DisplayName("Courier можно обновить и восстановить с новой локацией")
    void shouldUpdateCourier() {
        var courier = createCourier();
        var newLocation = Location.create(7, 8).getValue();

        courierRepository.add(courier);
        courier.moveTo(newLocation);
        courierRepository.update(courier);
        entityManager.flush();
        entityManager.clear();

        var result = courierRepository.getById(courier.getId());

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getLocation()).isEqualTo(newLocation);
    }

    /**
     * Сценарий: repository возвращает всех Courier.
     * <p>
     * Given: сохранены два Courier.
     * When: вызывается getAll.
     * Then: возвращаются оба Courier вместе с загруженными назначениями.
     */
    @Test
    @DisplayName("Repository возвращает всех Courier")
    void shouldReturnAllCouriers() {
        var firstCourier = createCourier();
        var secondCourier = createCourier();
        secondCourier.assign(UUID.randomUUID(), Volume.create(2).getValue(), Location.create(6, 5).getValue());

        courierRepository.add(firstCourier);
        courierRepository.add(secondCourier);
        entityManager.flush();
        entityManager.clear();

        var couriers = courierRepository.getAll();

        assertThat(couriers).hasSize(2);
        assertThat(couriers).extracting(courier -> courier.getAssignments().size()).containsExactlyInAnyOrder(0, 1);
    }

    /**
     * Сценарий: разные агрегаты можно сохранить в одной транзакции и затем откатить.
     * <p>
     * Given: созданы Order и Courier.
     * When: сохранение выполняется в тестовой транзакции и затем откатывается.
     * Then: ни один из агрегатов не оказывается в базе после завершения транзакции.
     */
    @Test
    @DisplayName("Разные агрегаты можно сохранить в одной транзакции и затем откатить")
    void shouldRollbackMultipleAggregatesInOneTransaction() {
        var order = createOrder();
        var courier = createCourier();

        orderRepository.add(order);
        courierRepository.add(courier);
        TestTransaction.flagForRollback();
        TestTransaction.end();

        assertThat(orderRepository.getById(order.getId()).isFailure()).isTrue();
        assertThat(courierRepository.getById(courier.getId()).isFailure()).isTrue();
    }

    private Order createOrder() {
        return Order.create(
                UUID.randomUUID(),
                Location.create(5, 5).getValue(),
                Volume.create(3).getValue()).getValue();
    }

    private Courier createCourier() {
        return Courier.create("Ivan", Location.create(5, 5).getValue()).getValue();
    }
}
