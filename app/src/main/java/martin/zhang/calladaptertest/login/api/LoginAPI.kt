package martin.zhang.calladaptertest.login.api

import com.squareup.moshi.JsonClass
import martin.zhang.calladaptertest.login.data.TokenResponse
import martin.zhang.calladaptertest.network.core.ApiResult
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginAPI {
    @POST("login")
    suspend fun login(@Body loginInput: LoginInput): ApiResult<TokenResponse>
}

@JsonClass(generateAdapter = true)
data class LoginInput(
    val device: String = "mobile",
    val password: String,
    val userId: String,
    val mrktCd: String,
    val langCd: String,
    val appData: LoginSysInfoInput
)

@JsonClass(generateAdapter = true)
data class LoginSysInfoInput(
    val os: String?, val osVersion: String?,
    val deviceId: String?,
    val appVersion: String?,
    val deviceName: String?
)