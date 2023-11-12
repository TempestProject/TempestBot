package cloud.drakon.tempestbot.interact.api.openai.images

import kotlinx.serialization.Serializable

@Serializable class ImageResponse(
    val created: Int,
    val data: Array<Image>,
)
