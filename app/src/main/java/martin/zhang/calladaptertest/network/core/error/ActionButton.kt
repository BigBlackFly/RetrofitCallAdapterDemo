package martin.zhang.calladaptertest.network.core.error

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable


@JsonClass(generateAdapter = true)
data class ActionButton(
    @Json(name = "type") var type: String = "",
    @Json(name = "label") var label: String = "",
    @Json(name = "action") var action: String = ""
) : Serializable
