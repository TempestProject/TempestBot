@file:OptIn(ExperimentalSerializationApi::class)

package cloud.drakon.tempestbot.interact.api.openai.images

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class ImageRequest(
    val prompt: String,
    @EncodeDefault val model: String = "dall-e-3",
    @EncodeDefault @SerialName("response_format")
    val responseFormat: String = "b64_json",
    @EncodeDefault val size: String = "1792x1024",
    val style: String? = null,
)
