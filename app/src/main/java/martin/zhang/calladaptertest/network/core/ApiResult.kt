package martin.zhang.calladaptertest.network.core

import martin.zhang.calladaptertest.network.core.error.ErrorAPIResponseModel


sealed class ApiResult<out T> {
    // we don't need it. please just simply show a loading animation when we start a network request.
    class Loading : ApiResult<Nothing>()

    // success
    class Success<T>(val data: T) : ApiResult<T>()

    // exception from server side,which means response code is not 2xx. in this case, server will send us an ErrorAPIResponseModel.
    class Failure(val error: ErrorAPIResponseModel) : ApiResult<Nothing>()

    // exception from our side.
    class Exception(val exception: java.lang.Exception) : ApiResult<Nothing>()
}

