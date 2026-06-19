package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CompleteOrderCommand {

    private final UUID courierId;
    private final UUID orderId;

    public static Result<CompleteOrderCommand, Error> create(UUID courierId, UUID orderId) {
        var err = Guard.combine(Guard.againstNullOrEmpty(courierId, "courierId"),
                Guard.againstNullOrEmpty(orderId, "orderId"));
        if (err != null) {
            return Result.failure(err);
        }

        return Result.success(new CompleteOrderCommand(courierId, orderId));
    }
}
