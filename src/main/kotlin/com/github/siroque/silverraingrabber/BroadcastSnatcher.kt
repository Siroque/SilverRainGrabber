package com.github.siroque.silverraingrabber

import com.github.siroque.silverraingrabber.utils.DateTimeUtils.reformatDateString
import com.github.siroque.silverraingrabber.utils.FileUtilities.dropDirectoryContent
import com.github.siroque.silverraingrabber.utils.FileUtilities.flushFile
import com.github.siroque.silverraingrabber.utils.FileUtilities.makeSureDirectoryExists
import com.github.siroque.silverraingrabber.Mp3TagWriter.writeTags
import com.github.siroque.silverraingrabber.parsers.WebPageExtractor.getBroadcast
import com.github.siroque.silverraingrabber.parsers.WebPageExtractor.getBroadcastListItems
import com.github.siroque.silverraingrabber.models.Broadcast
import com.github.siroque.silverraingrabber.models.BroadcastCategory
import com.github.siroque.silverraingrabber.parsers.WebPageExtractor.getBroadcastCategoryListItems
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.util.logging.Logger

object BroadcastSnatcher {
    private var logger: Logger = Logger.getLogger("com.github.siroque.silverraingrabber.BroadcastSnatcher")
    val baseUrl = "https://www.silver.ru"

    fun fetch(rootDirectoryPath: String) {
        makeSureDirectoryExists(rootDirectoryPath)
        val categoryItems = getBroadcastCategoryListItems(baseUrl, "/programms")
        categoryItems.forEachIndexed { index, category ->
            println("Progress: ${categoryItems.size/100.0*index}%")
            fetchBroadcasts(baseUrl, category, rootDirectoryPath)
        }
    }

    fun fetchBroadcasts(baseUrl: String, category: BroadcastCategory, rootDirectoryPath: String): ArrayList<Broadcast> {
        val client = OkHttpClient()
        val completeBroadcasts = ArrayList<Broadcast>()
        val categoryOutputPath = outputPathForCategory(category, rootDirectoryPath)
        makeSureDirectoryExists(categoryOutputPath)
        dropDirectoryContent(categoryOutputPath)

        var endOfFeedReached = false
        var pageIndex = 0
        var latestBroadcastDate = ""
        while (!endOfFeedReached) {
            if(endOfFeedReached) continue
            val pageUrl = category.url + "?PAGEN_1=$pageIndex&PAGEN_1=${pageIndex+1}"
            val broadcastListItems = getBroadcastListItems(baseUrl, pageUrl)
            if (broadcastListItems.any { item -> item.date ==  latestBroadcastDate}) {
                endOfFeedReached = true
            } else {
                broadcastListItems.forEachIndexed { itemIndex, listItem ->
                    if(listItem.date != latestBroadcastDate) {
                        logger.info("$itemIndex       Downloading from ${listItem.date} ${listItem.title}")
                        getBroadcast(baseUrl, listItem, category)?.let { broadcast ->
                            val mp3FilePath = fetchMp3File(broadcast, categoryOutputPath, client)
                            completeBroadcasts.add(
                                Broadcast(
                                    broadcast.title,
                                    category.hostNames,
                                    broadcast.category,
                                    broadcast.categoryUrl,
                                    broadcast.date,
                                    broadcast.description,
                                    broadcast.url,
                                    broadcast.streamUrl,
                                    broadcast.imgUrl,
                                    mp3FilePath
                                )
                            )
                        }
                    }
                    if(pageIndex == 0 && itemIndex == 0) latestBroadcastDate = listItem.date
                }
            }
            pageIndex++
        }
        return completeBroadcasts
    }

    private fun outputPathForCategory(category: BroadcastCategory, rootDirectoryPath: String): String {
        return "${rootDirectoryPath}\\${category.title}"
    }

    private fun fetchMp3File(
        broadcast: Broadcast,
        outputDirectoryPath: String,
        client: OkHttpClient): String? {
        val request = Request.Builder().url(broadcast.streamUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) logger.severe("Failed to fetch audio file for broadcast $broadcast")
            else {
                val targetMp3Path = filePathForBroadcast(broadcast, outputDirectoryPath)
                if (writeBroadcastToFile(response.body!!.byteStream(), targetMp3Path)){
                    val imageStream = fetchImageStream(broadcast.imgUrl, client)
                    if(writeTags(broadcast, imageStream, targetMp3Path)) {
                        return targetMp3Path
                    }
                }
            }
        }
        return null
    }

    private fun fetchImageStream(imgUrl: String?, client: OkHttpClient): ByteArray? {
        imgUrl?.let{
            logger.info("Start fetching image from url: $imgUrl")
            val request = Request.Builder().url(imgUrl).build()
            client.newCall(request).execute().use { response ->
                return if (!response.isSuccessful) null
                else response.body!!.byteStream().readBytes()
            }
        }
        return null
    }

    private fun writeBroadcastToFile(inputStream: InputStream, outputFilePath: String): Boolean {
        val outputFile = flushFile(outputFilePath)
        logger.info("   Going to store audio file at path: ${outputFile.absolutePath}")
        val outputStream = outputFile.outputStream()

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        return outputFile.exists()
    }

    private fun filePathForBroadcast(broadcast: Broadcast, rootOutputDirectoryPath: String): String {
        val date = reformatDateString(broadcast.date, "dd MMMM yyyy HH:mm", "yyyyMMdd_HHmm")
        val legalFilePath = (date + " " + broadcast.title).replace(Regex("[:\\\\/*\"?|<>]"), "_")
        return "$rootOutputDirectoryPath/${legalFilePath}.mp3"
    }
}