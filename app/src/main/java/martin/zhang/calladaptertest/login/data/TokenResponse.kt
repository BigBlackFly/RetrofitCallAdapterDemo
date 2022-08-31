package martin.zhang.calladaptertest.login.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class TokenResponse(
    @Json(name = "accessToken") val accessToken: String = "",
    @Json(name = "firstLoginPage") val firstLoginPage: Boolean = false,
    @Json(name = "refreshToken") val refreshToken: String? = "",
    @Json(name = "expireDate") val expireDate: String? = "",
    @Json(name = "expiresIn") val expiresIn: Long = 0,
    @Json(name = "refreshExpireDate") val refreshExpireDate: String? = "",
    @Json(name = "refreshExpiresIn") val refreshExpiresIn: Long = 0,
    @Json(name = "diffLangFlag") val diffLangFlag: Boolean = false,
    @Json(name = "diffLangCd") val diffLangCd: String = "",
    @Json(name = "userId") val userId: String = "",
    @Json(name = "acctNr") val acctNr: String = ""
)
