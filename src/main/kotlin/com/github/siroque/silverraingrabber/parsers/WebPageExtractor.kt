package com.github.siroque.silverraingrabber.parsers

import com.github.siroque.silverraingrabber.models.Broadcast
import com.github.siroque.silverraingrabber.models.BroadcastCategory
import com.github.siroque.silverraingrabber.models.BroadcastListItem
import org.jsoup.Jsoup
import java.util.logging.Logger

object WebPageExtractor {
    private var logger: Logger = Logger.getLogger("com.github.siroque.silverraingrabber.parsers.WebPageExtractor")

    fun getBroadcastCategoryListItems(baseUrl: String, broadcastCategoryListPath: String): List<BroadcastCategory> {
        val broadcastCategories = mutableListOf<BroadcastCategory>()
        logger.info("Getting category list items from page ${baseUrl+broadcastCategoryListPath}")
        Jsoup.connect(baseUrl+broadcastCategoryListPath).get().run {
            select("div.program-list table tbody tr ").forEach { element ->
                val categoryNameAnchor = element.select("td h3 a")
                val categoryTitle = categoryNameAnchor.text()
                val categoryUrl = categoryNameAnchor.attr("href")

                val categoryHosts = mutableListOf<String>()
                element.select("td ul li h4 a").forEach { categoryHosts.add(it.text()) }
                broadcastCategories.add(BroadcastCategory(categoryTitle, baseUrl+categoryUrl, categoryHosts))
            }
        }
        return broadcastCategories
    }

    fun getBroadcastListItems(baseUrl: String, broadcastListUrl: String): List<BroadcastListItem> {
        val broadcasts = mutableListOf<BroadcastListItem>()
        logger.info("Getting broadcast list items from page $broadcastListUrl")
        Jsoup.connect(broadcastListUrl).get().run {
            select("tr ").forEach { element ->
                val headerAnchor = element.select("td h4 a")
                val title = headerAnchor.text()
                val href = headerAnchor.attr("href")

                val imgAnchor = element.select("td a img")
                var imgSrc: String? = imgAnchor.attr("src")
                if (imgSrc!!.isBlank()) imgSrc = null

                val dateAnchor = element.select("td div span")
                val date = dateAnchor.text()

                val descriptionAnchor = element.select("td p")
                val description = descriptionAnchor.text()
                if (title.isNotEmpty() && date.isNotEmpty() && href.isNotEmpty()){
                    var desc = ""
                    description?.let { desc = it }
                    broadcasts.add(
                        BroadcastListItem(
                            title,
                            baseUrl + href,
                            date,
                            desc,
                            baseUrl + imgSrc
                        )
                    )
                }
            }
        }
        logger.info("Extracted ${broadcasts.size} broadcast list items")
        return broadcasts
    }

    fun getBroadcast(baseUrl: String, item: BroadcastListItem, category: BroadcastCategory): Broadcast? {
        try {
            Jsoup.connect(item.url).get().run {
                select("audio").forEach { element ->
                    val streamUrl = element.attr("src")

                    if (streamUrl.isNotEmpty() && (streamUrl.contains("medialibrary")
                                || streamUrl.contains("LoadedImages")
                                || streamUrl.contains("LoadedFiles"))){
                        return Broadcast(
                            item.title,
                            category.hostNames,
                            category.title,
                            baseUrl + category.url,
                            item.date,
                            item.description,
                            item.url,
                            baseUrl + streamUrl,
                            item.imgUrl,
                            null
                        )
                    } else {
                        logger.warning("ERROR: Malformed src attribut (streamUrl) of audio player: $streamUrl")
                        logger.warning("`audio` css selector resolver to element with content: $element")
                    }
                }
            }
        } catch (e: Exception) {
            logger.severe("Failed to fetch broadcast from URL: ${item.url}. Error message: ${e.localizedMessage}")
        }
        return null
    }
}