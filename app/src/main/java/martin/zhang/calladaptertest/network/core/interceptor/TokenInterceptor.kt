package martin.zhang.calladaptertest.network.core.interceptor

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.nio.charset.Charset


class TokenInterceptor(
    @Volatile
    var token: String,
    @Volatile
    var refreshToken: String
) : Interceptor {

    companion object {
        const val ACCESS_TOKEN_NAME = "accessToken"
        const val REFRESH_TOKEN_NAME = "refreshToken"
        const val CODE_NAME = "code"
        const val TAG = "TokenInterceptor"
    }

    private fun updateTokenAndRefreshToken(token: String, refreshToken: String) {
        this.token = token
        this.refreshToken = refreshToken
    }

    private val refreshTokenUrl = "login/updateToken"

    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d(TAG, "intercept")
        val builder = chain.request().newBuilder()
        builder.header(ACCESS_TOKEN_NAME, token)
        val outDateToken = token
        val response = chain.proceed(builder.build())

        // 先判断请求回来的数据是否过期
        if (isTokenExpired(response)) {
            // 请求并发的情况下会有多个线程进入这个分支
            // refreshToken 同步请求，确保后续请求被阻塞
            refreshToken(outDateToken)
            val newBuilder = chain.request().newBuilder()
            newBuilder.header(ACCESS_TOKEN_NAME, token)
            return chain.proceed(newBuilder.build())
        }
        return response
    }

    private fun isTokenExpired(response: Response): Boolean {
        // Response.body.string 只能调用一次
        val string = copyBuffer(response.body)
        return getCode(string) == 401
    }

    private fun getCode(string: String?): Int {
        return string?.let {
            JSONObject(string).getInt(CODE_NAME)
        } ?: error("body null")
    }

    private fun copyBuffer(body: ResponseBody?): String? {
        val source = body?.source()
        source?.request(Long.MAX_VALUE)
        val buffer = source?.buffer
        return buffer?.clone()?.readString(Charset.defaultCharset())
    }

    /**
     * [outDateToken] 网络请求保存的token
     * 用于判断token是否被刷新
     * token过期进入该函数时会竞争，后续会被阻塞，当 token刷新完毕时
     * 再次判断，如果之前保存的[outDateToken]和全局[token]相等，则是过期token，刷新就行
     * 不相等则说明保存的[outDateToken]是没有过期的，携带进行请求
     */
    @Synchronized
    private fun refreshToken(outDateToken: String): String {
        if (token.isEmpty() || outDateToken == token) {
            Log.d(TAG, "refreshToken: start token = $token")
            val client = OkHttpClient()
            val builder = Request.Builder()
            val call = client.newCall(
                builder.get()
                    .url("$refreshTokenUrl?refreshToken=$refreshToken")
                    .build()
            )
            Log.d(TAG, "refreshToken: call start")
            //OKHttp同步请求
            updateTokenByResponse(call.execute())
            Log.d(TAG, "refreshToken: call end")
            Log.d(TAG, "refreshToken: end token = $token")
        }
        return token
    }

    private fun updateTokenByResponse(response: Response) {
        response.code.also {
            if (it == 200) {
                val string = response.body.string()
                JSONObject(string).let { json ->
                    val code = json.getInt(CODE_NAME)
                    Log.d(TAG, "updateTokenByResponse: code = $code")
                    if (code == 200) {
                        val data = json.getJSONObject("data")
                        Log.d(TAG, "updateTokenByResponse: sleep")
                        updateTokenAndRefreshToken(
                            data.getString(ACCESS_TOKEN_NAME),
                            data.getString(REFRESH_TOKEN_NAME)
                        )
                        return
                    }
                }
            }
            //todo new login
        }
    }
}