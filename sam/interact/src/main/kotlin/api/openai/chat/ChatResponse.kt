package cloud.drakon.tempestbot.interact.api.openai.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable class ChatResponse(
    val id: String,
    val choices: Array<ChatChoice>,
    val created: Int,
    val model: String,
    @SerialName("system_fingerprint") val systemFingerprint: String? = null,
    @SerialName("object") val chatObject: String,
    val usage: ChatUsage,
)
