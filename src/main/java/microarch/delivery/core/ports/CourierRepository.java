package microarch.delivery.core.ports;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.UUID;

public interface CourierRepository {

    Courier add(Courier courier);

    Courier update(Courier courier);

    Result<Courier, Error> getById(UUID id);

    List<Courier> getAll();
}
