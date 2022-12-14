package cloud.drakon.tempestbot.interact.commands.ffxiv

import cloud.drakon.ktdiscord.channel.embed.Embed
import cloud.drakon.ktdiscord.channel.embed.EmbedField
import cloud.drakon.ktdiscord.channel.embed.EmbedThumbnail
import cloud.drakon.ktdiscord.interaction.Interaction
import cloud.drakon.ktdiscord.interaction.applicationcommand.ApplicationCommandData
import cloud.drakon.ktdiscord.webhook.EditWebhookMessage
import cloud.drakon.ktuniversalis.KtUniversalisClient
import cloud.drakon.tempestbot.interact.Handler
import cloud.drakon.tempestbot.interact.api.xivapi.XivApiClient
import com.amazonaws.services.lambda.runtime.LambdaLogger
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist

suspend fun universalis(
    event: Interaction<ApplicationCommandData>,
    logger: LambdaLogger,
) {
    logger.log("Responding to Universalis command")

    lateinit var item: String
    lateinit var world: String
    var highQuality: Boolean? = null

    when (event.data !!.type) {
        1 -> for (i in event.data !!.options !!) {
            when (i.name) {
                "item" -> item = i.value !!
                "world" -> world = i.value !!
                "high_quality" -> highQuality = i.value !!.toBooleanStrict()
            }
        }

        else -> logger.log("Unknown application command type: " + event.data !!.type)
    }

    val ktorClient = HttpClient(Java)
    val xivApi = XivApiClient(ktorClient = ktorClient)

    val xivApiItemId = xivApi.search(
        item, "Item"
    ).jsonObject["Results"] !!.jsonArray[0].jsonObject["ID"] !!.jsonPrimitive.int
    val xivApiItem = xivApi.item(xivApiItemId)

    val canBeHighQuality: Boolean =
        xivApiItem.jsonObject["CanBeHq"] !!.jsonPrimitive.int == 1
    val description: String = Jsoup.clean(
        xivApiItem.jsonObject["Description"] !!.jsonPrimitive.content,
        "",
        Safelist.none(),
        Document.OutputSettings().prettyPrint(false)
    )

    val marketBoardCurrentData = if (highQuality == true && canBeHighQuality) {
        KtUniversalisClient.getMarketBoardCurrentData(
            world,
            arrayOf(xivApiItemId).toIntArray(),
            entries = 5,
            listings = 5,
            hq = true
        )
    } else if (highQuality == false) {
        KtUniversalisClient.getMarketBoardCurrentData(
            world,
            arrayOf(xivApiItemId).toIntArray(),
            entries = 5,
            listings = 5,
            hq = false
        )
    } else {
        KtUniversalisClient.getMarketBoardCurrentData(
            world, arrayOf(xivApiItemId).toIntArray(), entries = 5, listings = 5
        )
    }
    val marketBoardListings = marketBoardCurrentData.listings
    var listings = ""
    var totalPrices = ""
    val gil = "<:gil:235457032616935424>"

    if (! marketBoardListings.isNullOrEmpty()) {
        for (i in marketBoardListings) {
            listings += String.format(
                "%,d", i.pricePerUnit
            ) + " $gil x " + i.quantity + " [" + i.worldName + "]" + if (i.hq) {
                " <:hq:916051971063054406>\n"
            } else {
                "\n"
            }
            totalPrices += String.format(
                "%,d", i.total
            ) + " $gil\n"
        }
    } else {
        listings = "None"
        totalPrices = "N/A"
    }

    Handler.ktDiscordClient.editOriginalInteractionResponse(
        EditWebhookMessage(
            embeds = arrayOf(
                Embed(
                    title = "Current prices for " + xivApiItem.jsonObject["Name"] !!.jsonPrimitive.content,
                    description = description,
                    url = "https://universalis.app/market/$xivApiItemId",
                    thumbnail = EmbedThumbnail("https://xivapi.com" + xivApiItem.jsonObject["IconHD"] !!.jsonPrimitive.content),
                    fields = arrayOf(
                        if (highQuality == true && canBeHighQuality && (marketBoardCurrentData.currentAveragePriceHq > 0)) {
                            EmbedField(
                                name = "Current average price (HQ)",
                                value = String.format(
                                    "%,f", marketBoardCurrentData.currentAveragePriceHq
                                ).trimEnd('0') + " $gil",
                                inline = false
                            )
                        } else if (highQuality == false && (marketBoardCurrentData.currentAveragePriceNq > 0)) {
                            EmbedField(
                                name = "Current average price (NQ)",
                                value = String.format(
                                    "%,f", marketBoardCurrentData.currentAveragePriceNq
                                ).trimEnd('0') + " $gil",
                                inline = false
                            )
                        } else if (highQuality == null && (marketBoardCurrentData.currentAveragePrice > 0)) {
                            EmbedField(
                                name = "Current average price", value = String.format(
                                    "%,f", marketBoardCurrentData.currentAveragePrice
                                ).trimEnd('0') + " $gil", inline = false
                            )
                        } else {
                            EmbedField(
                                name = "Current average price",
                                value = "N/A",
                                inline = false
                            )
                        },
                        if (highQuality == true && canBeHighQuality && (marketBoardCurrentData.averagePriceHq > 0)) {
                            EmbedField(
                                name = "Historic average price (HQ)",
                                value = String.format(
                                    "%,f", marketBoardCurrentData.averagePriceHq
                                ).trimEnd('0') + " $gil",
                                inline = false
                            )
                        } else if (highQuality == false && (marketBoardCurrentData.averagePriceNq > 0)) {
                            EmbedField(
                                name = "Historic average price (NQ)",
                                value = String.format(
                                    "%,f", marketBoardCurrentData.averagePriceNq
                                ).trimEnd('0') + " $gil",
                                inline = false
                            )
                        } else if (highQuality == null && (marketBoardCurrentData.averagePrice > 0)) {
                            EmbedField(
                                name = "Historic average price", value = String.format(
                                    "%,f", marketBoardCurrentData.averagePrice
                                ).trimEnd('0') + " $gil", inline = false
                            )
                        } else {
                            EmbedField(
                                name = "Historic average price",
                                value = "N/A",
                                inline = false
                            )
                        },
                        EmbedField(
                            name = "Listings", value = listings, inline = true
                        ),
                        EmbedField(
                            name = "Total price", value = totalPrices, inline = true
                        )
                    )
                )
            )
        ), event.token
    )
}
