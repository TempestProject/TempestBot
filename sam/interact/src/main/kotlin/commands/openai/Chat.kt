package cloud.drakon.tempestbot.interact.commands.openai

import cloud.drakon.ktdiscord.interaction.Interaction
import cloud.drakon.ktdiscord.interaction.applicationcommand.ApplicationCommandData
import cloud.drakon.ktdiscord.webhook.EditWebhookMessage
import cloud.drakon.ktdiscord.webhook.ExecuteWebhook
import cloud.drakon.tempestbot.interact.Handler.Companion.json
import cloud.drakon.tempestbot.interact.Handler.Companion.ktDiscord
import cloud.drakon.tempestbot.interact.Handler.Companion.mongoDatabase
import cloud.drakon.tempestbot.interact.Handler.Companion.openAi
import cloud.drakon.tempestbot.interact.api.openai.chat.ChatRequest
import cloud.drakon.tempestbot.interact.api.openai.chat.Message
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.bson.BsonArray
import org.bson.BsonDocument

suspend fun chat(event: Interaction<ApplicationCommandData>) = coroutineScope {
    lateinit var message: String
    var thread: String? = null

    for (i in event.data !!.options !!) {
        when (i.name) {
            "message" -> message = i.value !!
            "thread" -> thread = i.value !!
        }
    }

    val mongoCollection = mongoDatabase.getCollection("chat")

    val messages: MutableList<Message>
    val newMessage = Message("user", message)
    if (thread != null) {
        val mongoThread = mongoCollection.find(
            Filters.and(
                Filters.eq("guild_id", event.guildId), Filters.eq("thread", thread)
            )
        ).projection(
            Projections.fields(
                Projections.include("messages"), Projections.excludeId()
            )
        ).first()

        if (mongoThread != null) {
            messages =
                (mongoThread["messages"] as ArrayList<*>).filterIsInstance<Message>()
                    .toMutableList()
            messages.add(newMessage)
        } else {
            messages = mutableListOf(newMessage)
        }
    } else {
        messages = mutableListOf(newMessage)
    }

    val chatGpt = openAi.createChatCompletion(
        ChatRequest(
            "gpt-4",
            messages.toTypedArray(),
            maxTokens = 2000
        )
    ).choices[0].message

    if (thread != null) {
        val newMessages: Array<Message> = arrayOf(newMessage, chatGpt)
        val document = BsonArray()
        for (i in newMessages) {
            document.add(
                BsonDocument.parse(
                    json.encodeToString(
                        Message.serializer(), i
                    )
                )
            )
        }

        launch {
            mongoCollection.updateOne(
                Filters.and(
                    Filters.eq("guild_id", event.guildId), Filters.eq("thread", thread)
                ), Updates.addEachToSet(
                    "messages", document
                ), UpdateOptions().upsert(true)
            )
        }
    }

    launch {
        if (chatGpt.content.length <= 2000) {
            ktDiscord.editOriginalInteractionResponse(
                EditWebhookMessage(
                    content = chatGpt.content
                ), event.token
            )
        } else {
            val chatGptChunked = chatGpt.content.chunked(2000)

            launch {
                ktDiscord.editOriginalInteractionResponse(
                    EditWebhookMessage(
                        content = chatGptChunked[0]
                    ), event.token
                )
            }

            launch {
                for (i in chatGptChunked.drop(0)) {
                    ktDiscord.createFollowupMessage(
                        ExecuteWebhook(content = i), event.token
                    )
                }
            }
        }
    }
}
