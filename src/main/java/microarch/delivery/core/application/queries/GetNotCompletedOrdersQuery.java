package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;

public final class GetNotCompletedOrdersQuery {

    private GetNotCompletedOrdersQuery() {
    }

    public static Result<GetNotCompletedOrdersQuery, Error> create() {
        return Result.success(new GetNotCompletedOrdersQuery());
    }
}
