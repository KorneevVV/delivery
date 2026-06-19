package microarch.delivery.core.application.queries.dto;

import java.util.UUID;

public record OrderDto(UUID id, LocationDto location) {
}
