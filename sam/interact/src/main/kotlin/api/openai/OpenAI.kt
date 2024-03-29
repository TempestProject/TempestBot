package cloud.drakon.tempestbot.interact.api.openai

import cloud.drakon.tempestbot.interact.api.openai.chat.ChatRequest
import cloud.drakon.tempestbot.interact.api.openai.chat.ChatResponse
import cloud.drakon.tempestbot.interact.api.openai.images.ImageRequest
import cloud.drakon.tempestbot.interact.api.openai.images.ImageResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class OpenAI(private val apiKey: String) {
    private val ktorClient = HttpClient(Java) {
        defaultRequest {
            url("https://api.openai.com/v1/")
            contentType(ContentType.Application.Json)
        }

        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(apiKey, apiKey)
                }
            }
        }

        install(ContentNegotiation) {
            json(Json { encodeDefaults = false })
        }
    }

    suspend fun createChatCompletion(request: ChatRequest): ChatResponse {
        return ktorClient.post("chat/completions") {
            setBody(request)
        }.body()
    }

    suspend fun createImage(request: ImageRequest): ImageResponse {
        return ktorClient.post("images/generations") {
            setBody(request)
        }.body()
    }
}
