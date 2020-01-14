package com.github.siroque.silverraingrabber

import com.github.siroque.silverraingrabber.utils.DateTimeUtils.reformatDateString
import com.github.siroque.silverraingrabber.models.Broadcast
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagOptionSingleton
import org.jaudiotagger.tag.id3.ID3v24FieldKey
import org.jaudiotagger.tag.id3.ID3v24Frames.*
import org.jaudiotagger.tag.id3.ID3v24Tag
import org.jaudiotagger.tag.id3.framebody.*
import org.jaudiotagger.tag.reference.ID3V2Version
import java.io.File
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger

object Mp3TagWriter {
    private var logger: Logger = Logger.getLogger("com.github.siroque.silverraingrabber.Mp3TagWriter")

    fun writeTags(broadcast: Broadcast, imageStream: ByteArray?, filePath: String): Boolean {
        try {
            val file = File(filePath)
            val mp3File = AudioFileIO.read(file) as MP3File
            TagOptionSingleton.getInstance().iD3V2Version = ID3V2Version.ID3_V24
            val id3v2Tag: ID3v24Tag = mp3File.tagAndConvertOrCreateAndSetDefault as ID3v24Tag

            addTagField(ID3v24FieldKey.TITLE, broadcast.title, id3v2Tag)
            addTagField(ID3v24FieldKey.ALBUM, broadcast.category, id3v2Tag)
            addTagField(
                ID3v24FieldKey.ARTIST,
                broadcast.hostNames.toTypedArray()
                    .contentToString()
                    .replace("[", "")
                    .replace("]", ""),
                id3v2Tag
            )
            addTagField(ID3v24FieldKey.COMMENT, broadcast.description, id3v2Tag)
            addArtistUrl(broadcast.categoryUrl, id3v2Tag)
            addBroadcastSourceUrl(broadcast.url, id3v2Tag)
            addPublisher("Радиостанция «Серебряный Дождь»", id3v2Tag)
            addPublisherUrl(URI(broadcast.url).host, id3v2Tag)
            addRadioUrl(URI(broadcast.url).host, id3v2Tag)
            addAudioFileUrl(broadcast.streamUrl, id3v2Tag)
            addCopyrightInfo("Радиостанция «Серебряный Дождь»", id3v2Tag)
            addLanguage("Russian", id3v2Tag)
            addReleaseTime(broadcast.date, id3v2Tag)
            addTagField(
                ID3v24FieldKey.YEAR,
                reformatDateString(broadcast.date, "dd MMMM yyyy HH:mm", "yyyy"),
                id3v2Tag
            )
            imageStream?.let {
                id3v2Tag.deleteField(FieldKey.COVER_ART)
                id3v2Tag.addField(id3v2Tag.createArtworkField(imageStream, "image/jpeg"))
            }
            mp3File.tag = id3v2Tag
            mp3File.save()
            return true
        } catch (e: Exception){
            logger.info("Failed to write mp3 tags for file at path $filePath for broadcast $broadcast")
            logger.info(e.message)
            return false
        }
    }

    private fun addReleaseTime(date: String, tag: ID3v24Tag) {
        val inputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale("ru"))
        val frame = tag.createFrame(FRAME_ID_ORIGINAL_RELEASE_TIME)
        val body = FrameBodyTDOR()
        body.text = LocalDateTime.parse(date.toLowerCase(), inputFormatter).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        frame.body = body
        tag.removeFrame(FRAME_ID_ORIGINAL_RELEASE_TIME)
        tag.addFrame(frame)
    }

    private fun addLanguage(lang: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_LANGUAGE)
        val body = FrameBodyTLAN()
        body.text = lang
        frame.body = body
        tag.removeFrame(FRAME_ID_LANGUAGE)
        tag.addFrame(frame)
    }

    private fun addCopyrightInfo(info: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_COPYRIGHTINFO)
        val body = FrameBodyTCOP()
        body.text = info
        frame.body = body
        tag.removeFrame(FRAME_ID_COPYRIGHTINFO)
        tag.addFrame(frame)
    }

    private fun addBroadcastSourceUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_SOURCE_WEB)
        val body = FrameBodyWOAS()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_SOURCE_WEB)
        tag.addFrame(frame)
    }

    private fun addAudioFileUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_FILE_WEB)
        val body = FrameBodyWOAF()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_FILE_WEB)
        tag.addFrame(frame)
    }

    private fun addRadioUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_OFFICIAL_RADIO)
        val body = FrameBodyWORS()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_OFFICIAL_RADIO)
        tag.addFrame(frame)
    }

    private fun addPublisherUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_PUBLISHERS)
        val body = FrameBodyWPUB()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_PUBLISHERS)
        tag.addFrame(frame)
    }

    private fun addPublisher(name: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_PUBLISHER)
        val body = FrameBodyTPUB()
        body.text = name
        frame.body = body
        tag.removeFrame(FRAME_ID_PUBLISHER)
        tag.addFrame(frame)
    }

    private fun addArtistUrl(url: String, tag: ID3v24Tag) {
        val frame = tag.createFrame(FRAME_ID_URL_ARTIST_WEB)
        val body = FrameBodyWOAR()
        body.urlLink = url
        frame.body = body
        tag.removeFrame(FRAME_ID_URL_ARTIST_WEB)
        tag.addFrame(frame)
    }

    private fun addTagField(key: ID3v24FieldKey, value: String, tag: ID3v24Tag) {
        tag.deleteField(key)
        tag.addField(tag.createField(key, value))
    }
}