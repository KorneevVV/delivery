package microarch.delivery.core.application.queries;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAllCouriersQueryHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);

    /**
     * Сценарий: GetAllCouriersQueryHandler возвращает DTO всех Courier.
     * <p>
     * Given: repository возвращает Courier с именем и Location.
     * When: вызывается GetAllCouriersQueryHandler.handle.
     * Then: response содержит CourierDto с id, name и LocationDto без возврата доменного агрегата наружу.
     */
    @Test
    @DisplayName("GetAllCouriersQueryHandler возвращает DTO всех курьеров")
    void GetAllCouriersQueryHandler_ShouldReturnCourierDtos() {
        var courier = Courier.create("Ivan", Location.create(2, 3).getValue()).getValue();
        var handler = new GetAllCouriersQueryHandlerImpl(courierRepository);
        var query = GetAllCouriersQuery.create().getValue();
        when(courierRepository.getAll()).thenReturn(List.of(courier));

        var result = handler.handle(query);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().couriers()).hasSize(1);
        var dto = result.getValue().couriers().getFirst();
        assertThat(dto.id()).isEqualTo(courier.getId());
        assertThat(dto.name()).isEqualTo("Ivan");
        assertThat(dto.location().x()).isEqualTo(2);
        assertThat(dto.location().y()).isEqualTo(3);
    }
}
