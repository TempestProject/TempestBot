package cloud.drakon.tempestbot.interact.commands.ffxiv.eorzeadatabase

import cloud.drakon.ktdiscord.interaction.Interaction
import cloud.drakon.ktdiscord.interaction.applicationcommand.ApplicationCommandData
import cloud.drakon.ktdiscord.webhook.EditWebhookMessage
import cloud.drakon.ktxivapi.KtXivApi
import cloud.drakon.ktxivapi.search.StringAlgo
import cloud.drakon.tempestbot.interact.Handler.Companion.ktDiscord
import cloud.drakon.tempestbot.interact.commands.ffxiv.eorzeadatabase.medicineMeal
import com.amazonaws.services.lambda.runtime.LambdaLogger
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

suspend fun eorzeaDatabase(
    event: Interaction<ApplicationCommandData>,
    logger: LambdaLogger,
) {
    logger.log("Responding to Eorzea Database command")

    lateinit var index: String
    lateinit var string: String

    for (i in event.data !!.options !!) {
        when (i.name) {
            "index" -> index = i.value !!
            "string" -> string = i.value !!
        }
    }

    val search = KtXivApi.search(
        string, indexes = listOf(index), stringAlgo = StringAlgo.fuzzy, limit = 1
    )
    val id = search.results[0].id
    val item = KtXivApi.getContentId(index, id)
    val description = Jsoup.clean(
        item["Description"] !!.jsonPrimitive.content,
        "",
        Safelist.none(),
        Document.OutputSettings().prettyPrint(false)
    ).replace("""\n{3,}""".toRegex(), "\n\n")
    val itemKind = item["ItemKind"] !!.jsonObject["ID"] !!.jsonPrimitive.int

    val embed = when (itemKind) {
        5 -> { // Medicines & Meals
            medicineMeal(item, description)
        }

        else -> {
            throw Throwable("Unknown item type: ${item["ItemKind"] !!.jsonObject["Name"] !!.jsonPrimitive.content}")
        }
    }

    ktDiscord.editOriginalInteractionResponse(
        EditWebhookMessage(embeds = arrayOf(embed)), event.token
    )
}