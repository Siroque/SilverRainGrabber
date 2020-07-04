package com.github.siroque.silverraingrabber

import com.github.siroque.silverraingrabber.utils.DateTimeUtils.reformatDateString
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
import java.io.File
import java.io.InputStream
import java.util.logging.Logger

object BroadcastSnatcher {
    private var logger: Logger = Logger.getLogger("com.github.siroque.silverraingrabber.BroadcastSnatcher")
    private const val baseUrl = "https://www.silver.ru"
    private const val lectureArchiveUrl = "https://www.silver.ru/admin_materials/arkhiv-lektsiy/"

    fun fetch(rootDirectoryPath: String) {
        makeSureDirectoryExists(rootDirectoryPath)
        val categoryItems = getBroadcastCategoryListItems(baseUrl, "/programms")
        val client = OkHttpClient()
        categoryItems.forEachIndexed { index, category ->
            println("Progress: ${categoryItems.size/100.0*index}% (${category.title})")
            if(category.title == "Архив лекций") {
//                fetchArchiveBroadcasts(baseUrl, category, rootDirectoryPath, client)
            } else {
                fetchBroadcasts(baseUrl, category, rootDirectoryPath, client)
            }
        }
    }

    private fun fetchArchiveBroadcasts(
        baseUrl: String,
        category: BroadcastCategory,
        rootDirectoryPath: String,
        client: OkHttpClient
    ) {
//        val broadcastListItems = getArchiveBroadcastListItems(baseUrl, category)

    }

    private fun fetchBroadcasts(
        baseUrl: String,
        category: BroadcastCategory,
        rootDirectoryPath: String,
        client: OkHttpClient
    ): ArrayList<Broadcast> {
        val completeBroadcasts = ArrayList<Broadcast>()
        val categoryOutputPath = outputPathForCategory(category, rootDirectoryPath)
        makeSureDirectoryExists(categoryOutputPath)
//        dropDirectoryContent(categoryOutputPath)

        var endOfFeedReached = false
        var pageIndex = 0
        var latestBroadcastDate = ""
        while (!endOfFeedReached) {
            if(endOfFeedReached) continue
            val pageUrl = category.url + "?PAGEN_1=$pageIndex&PAGEN_1=${pageIndex+1}"
            val broadcastListItems = getBroadcastListItems(baseUrl, pageUrl)
            if (broadcastListItems.isEmpty() || broadcastListItems.any { item -> item.date ==  latestBroadcastDate}
                || File(filePathForBroadcast(broadcastListItems.first().date, broadcastListItems.first().title, categoryOutputPath)).exists()) {
                endOfFeedReached = true
            } else {
                broadcastListItems.forEachIndexed { itemIndex, listItem ->
                    val broadcastFile = File(filePathForBroadcast(listItem.date, listItem.title, categoryOutputPath))
                    if(listItem.date != latestBroadcastDate && !broadcastFile.exists()) {
                            logger.info("   Downloading $listItem")
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
                    } else {
                        logger.info("   Skipping broadcast list item $listItem")
                    }
                    if(pageIndex == 0 && itemIndex == 0) latestBroadcastDate = listItem.date
                }
            }
            pageIndex++
        }
        return completeBroadcasts
    }

    private fun outputPathForCategory(category: BroadcastCategory, rootDirectoryPath: String): String {
        val legalCategoryPath = category.title.replace(Regex("[?|<>]"), "")
        return "${rootDirectoryPath}\\$legalCategoryPath"
    }

    private fun fetchMp3File(
        broadcast: Broadcast,
        outputDirectoryPath: String,
        client: OkHttpClient): String? {
        println(broadcast)
        val request = Request.Builder().url(broadcast.streamUrl).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) logger.severe("     Failed to fetch audio file for broadcast $broadcast")
            else {
                val targetMp3Path = filePathForBroadcast(broadcast.date, broadcast.title, outputDirectoryPath)
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
            logger.info("   Start fetching image from url: $imgUrl")
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

    private fun filePathForBroadcast(broadcastDate: String, broadcastTitle: String, rootOutputDirectoryPath: String): String {
        val date = reformatDateString(broadcastDate, "dd MMMM yyyy HH:mm", "yyyyMMdd_HHmm")
        val legalFileName= ("$date $broadcastTitle").replace(Regex("[:\\\\/*\"?|<>]"), "_")
        val legalPath = rootOutputDirectoryPath.replace(Regex("[?|<>]"), "")
        return "${legalPath}/${legalFileName}.mp3"
    }
}