@file:OptIn(ExperimentalEncodingApi::class)

package cloud.drakon.tempestbot.interact.commands.openai

import cloud.drakon.ktdiscord.channel.Attachment
import cloud.drakon.ktdiscord.file.File
import cloud.drakon.ktdiscord.interaction.Interaction
import cloud.drakon.ktdiscord.interaction.applicationcommand.ApplicationCommandData
import cloud.drakon.ktdiscord.webhook.EditWebhookMessage
import cloud.drakon.tempestbot.interact.Handler.Companion.ktDiscord
import cloud.drakon.tempestbot.interact.Handler.Companion.openAi
import cloud.drakon.tempestbot.interact.api.openai.images.ImageRequest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

suspend fun image(event: Interaction<ApplicationCommandData>) {
    lateinit var prompt: String
    var style: String? = null

    for (i in event.data!!.options!!) {
        when (i.name) {
            "prompt" -> prompt = i.value!!
            "style" -> style = i.value!!
        }
    }

    val createdImage = openAi.createImage(ImageRequest(prompt, style = style)).data[0]

    ktDiscord.editOriginalInteractionResponse(
        EditWebhookMessage(
            files = arrayOf(
                File(
                    id = "0",
                    filename = "${prompt}.png",
                    contentType = "image/png",
                    bytes = Base64.decode(createdImage.b64Json)
                )
            ), attachments = arrayOf(
                Attachment(
                    id = "0",
                    filename = "${prompt}.png",
                    description = createdImage.revisedPrompt,
                    ephemeral = true
                )
            )
        ), event.token
    )
}
