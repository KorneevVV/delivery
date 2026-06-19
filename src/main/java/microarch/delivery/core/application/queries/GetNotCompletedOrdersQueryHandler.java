package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;

public interface GetNotCompletedOrdersQueryHandler {

    Result<GetNotCompletedOrdersResponse, Error> handle(GetNotCompletedOrdersQuery query);
}
