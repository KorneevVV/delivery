package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.application.queries.dto.LocationDto;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.shared.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {

    private final CourierRepository courierRepository;

    public GetAllCouriersQueryHandlerImpl(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @Override
    public Result<GetAllCouriersResponse, Error> handle(GetAllCouriersQuery query) {
        var couriers = courierRepository.getAll().stream().map(this::mapToDto).toList();

        return Result.success(new GetAllCouriersResponse(couriers));
    }

    private CourierDto mapToDto(Courier courier) {
        return new CourierDto(courier.getId(), courier.getName(), mapToDto(courier.getLocation()));
    }

    private LocationDto mapToDto(Location location) {
        return new LocationDto(location.getX(), location.getY());
    }
}
