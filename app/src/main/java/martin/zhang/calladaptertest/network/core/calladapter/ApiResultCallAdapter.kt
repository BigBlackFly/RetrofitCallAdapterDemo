package martin.zhang.calladaptertest.network.core.calladapter

import android.util.Log
import com.google.gson.Gson
import martin.zhang.calladaptertest.network.core.ApiResult
import martin.zhang.calladaptertest.network.core.error.ErrorAPIResponseModel
import okhttp3.Request
import okio.Timeout
import retrofit2.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class ApiResultCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        /*凡是检测不通过的，直接抛异常，提示使用者返回值类型格式不对
        因为ApiResultCallAdapterFactory是使用者显式设置使用的*/


        //以下是检查是否是 Call<ApiResult<T>> 类型的returnType

        //检查returnType是否是Call<T>类型的
        check(getRawType(returnType) == Call::class.java) { "$returnType must be retrofit2.Call." }
        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }

        //取出Call<T> 里的T，检查是否是ApiResult<T>
        val apiResultType = getParameterUpperBound(0, returnType)
        check(getRawType(apiResultType) == ApiResult::class.java) { "$apiResultType must be ApiResult." }
        check(apiResultType is ParameterizedType) { "$apiResultType must be parameterized. Raw types are not supported" }

        //取出ApiResult<T>中的T 也就是API返回数据对应的数据类型
        val dataType = getParameterUpperBound(0, apiResultType)

        return ApiResultCallAdapter<Any>(dataType)
    }
}

class ApiResultCallAdapter<T : Any>(private val type: Type) : CallAdapter<T, Call<ApiResult<T>>> {
    override fun responseType(): Type = type

    /**
     * Call<T> -> Call<ApiResult<T>>
     */
    override fun adapt(call: Call<T>): Call<ApiResult<T>> {
        return ApiResultCall(call)
    }
}

class ApiResultCall<T : Any> constructor(private val delegate: Call<T>) : Call<ApiResult<T>> {
    companion object {
        const val TAG = "ApiResultCall"
    }

    private val gson by lazy { Gson() }

    /**
     * 该方法会被Retrofit处理suspend方法的代码调用，并传进来一个callback,如果你回调了callback.onResponse，那么suspend方法就会成功返回
     * 如果你回调了callback.onFailure那么suspend方法就会抛异常
     *
     * 所以我们这里的实现是永远回调callback.onResponse,只不过在请求成功的时候返回的是ApiResult.Success对象，
     * 在失败的时候返回的是ApiResult.Failure对象，这样外面在调用suspend方法的时候就不会抛异常，一定会返回ApiResult.Success 或 ApiResult.Failure
     */
    override fun enqueue(callback: Callback<ApiResult<T>>) {
        //delegate 是用来做实际的网络请求的Call<T>对象，网络请求的成功失败会回调不同的方法
        delegate.enqueue(object : Callback<T> {

            /**
             * 网络请求成功返回，会回调该方法（无论status code是不是200）
             */
            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) { // http status is in the range [200..300).
                    val apiResult = ApiResult.Success(response.body()!!)
                    callback.onResponse(this@ApiResultCall, Response.success(apiResult))
                } else { // http status error
                    var mErrorAPIResponseModel = ErrorAPIResponseModel()

                    try {
                        mErrorAPIResponseModel = gson.fromJson(
                            response.errorBody()?.string(),
                            ErrorAPIResponseModel::class.java
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "$e")
                    }

                    val failureApiResult = ApiResult.Failure(mErrorAPIResponseModel)
                    callback.onResponse(this@ApiResultCall, Response.success(failureApiResult))
                }

            }

            /**
             * 在网络请求中发生了异常，会回调该方法
             */
            override fun onFailure(call: Call<T>, t: Throwable) {
                val failureApiResult = ApiResult.Exception(java.lang.Exception(t))
                // by using out T, we can assign a child generic object to a parent generic object.
                callback.onResponse(this@ApiResultCall, Response.success(failureApiResult))
            }
        })
    }

    override fun clone(): Call<ApiResult<T>> = ApiResultCall(delegate.clone())

    override fun execute(): Response<ApiResult<T>> {
        throw UnsupportedOperationException("ApiResultCall does not support synchronous execution")
    }


    override fun isExecuted(): Boolean {
        return delegate.isExecuted
    }

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean {
        return delegate.isCanceled
    }

    override fun request(): Request {
        return delegate.request()
    }

    override fun timeout(): Timeout {
        return delegate.timeout()
    }
}