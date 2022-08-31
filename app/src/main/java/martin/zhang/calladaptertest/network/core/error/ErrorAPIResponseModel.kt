package martin.zhang.calladaptertest.network.core.error

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import martin.zhang.calladaptertest.network.core.error.ActionButton

/**
 * Server Side Error
 *
 * Note: not all of the fields will be returned to us.
 */

@JsonClass(generateAdapter = true)
data class ErrorAPIResponseModel(
    @Json(name = "errorTraceID") var errorTraceID: String = "",
    @Json(name = "errorCode") var errorCode: String = "",
    @Json(name = "errorButtonText") var errorButtonText: String = "",
    @Json(name = "provider") var provider: String? = "",
    @Json(name = "authError") var authError: Boolean = false,
    @Json(name = "label") var label: String? = "",
    @Json(name = "type") var type: String? = "",
    @Json(name = "subType") var subType: String? = "",
    @Json(name = "heading") var heading: String? = "",
    @Json(name = "body") var body: String? = "",
    @Json(name = "buttons") var buttons: MutableList<ActionButton> = mutableListOf()
)